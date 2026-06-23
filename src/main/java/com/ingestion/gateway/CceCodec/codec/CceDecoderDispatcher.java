package com.ingestion.gateway.CceCodec.codec;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import com.ingestion.gateway.CceCodec.dto.CceIdentifier;
import com.ingestion.gateway.CceCodec.dto.CceMessageEnvelope;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CceDecoderDispatcher {

    private static final Logger LOG = Logger.getLogger(CceDecoderDispatcher.class);

    @Inject
    Instance<CceMessageCodec<?>> availableDecoders;

    private final Map<Integer, CceMessageCodec<?>> decoderRegistry = new HashMap<>();

    void onStart(@Observes StartupEvent ev) {
        for (CceMessageCodec<?> decoder : availableDecoders) {
            decoderRegistry.put(decoder.getSupportedParameterId(), decoder);
            LOG.infof("Registered STRICT CCE Decoder untuk Parameter ID: 0x%04X (%s)",
                    decoder.getSupportedParameterId(), decoder.getParameterName());
        }
    }

    public CceMessageEnvelope decode(byte[] rawData) {
        // 1. Validasi Struktur Dasar: Minimal harus ada "START ($$)", "SEPARATOR (,CCE,)", dan "END (* + CS + \r\n)"
        if (rawData == null || rawData.length < 15) {
            LOG.error("Payload terlalu pendek untuk menjadi pesan CCE yang valid.");
            return null;
        }

        // Cek Start Sign "$$" atau "@@" (Hex: 0x24 0x24 atau 0x40 0x40)
        if (!((rawData[0] == 0x24 && rawData[1] == 0x24) || (rawData[0] == 0x40 && rawData[1] == 0x40))) {
            LOG.error("Format pesan CCE tidak valid: Tidak diawali dengan $$ atau @@");
            return null;
        }

        // 2. Pencarian Checksum Marker '*' (0x2A) dan Validasi Checksum (Sesuai Manual Hal. 22)
        int asteriskIndex = -1;
        for (int i = rawData.length - 1; i >= 0; i--) {
            if (rawData[i] == 0x2A) {
                asteriskIndex = i;
                break;
            }
        }

        if (asteriskIndex == -1 || asteriskIndex + 2 >= rawData.length) {
            LOG.error("Pesan CCE korup: Marker '*' atau Checksum tidak ditemukan.");
            return null;
        }

        // Hitung Checksum dari '$$' hingga '*' (inklusif)
        int calculatedSum = 0;
        for (int i = 0; i <= asteriskIndex; i++) {
            calculatedSum += Byte.toUnsignedInt(rawData[i]);
        }
        byte expectedLowByteChecksum = (byte) (calculatedSum & 0xFF);

        // Ambil 2 byte setelah '*' sebagai hex string ASCII
        try {
            String receivedChecksumHex = new String(rawData, asteriskIndex + 1, 2, StandardCharsets.US_ASCII);
            int receivedChecksumValue = Integer.parseInt(receivedChecksumHex, 16);

            if (Byte.toUnsignedInt(expectedLowByteChecksum) != receivedChecksumValue) {
                LOG.errorf("CHECKSUM MISMATCH! Expected: %02X, Received: %02X. Paket di-drop.",
                        Byte.toUnsignedInt(expectedLowByteChecksum), receivedChecksumValue);
                return null;
            }
        } catch (NumberFormatException e) {
            LOG.error("Format Checksum pada ekor paket tidak valid (Bukan Hex ASCII).", e);
            return null;
        }

        // 3. Pencarian Separator Binary Header ",CCE,"
        byte[] cceMarker = ",CCE,".getBytes(StandardCharsets.US_ASCII);
        int payloadStartIndex = -1;
        for (int i = 0; i < asteriskIndex - cceMarker.length; i++) {
            boolean match = true;
            for (int j = 0; j < cceMarker.length; j++) {
                if (rawData[i + j] != cceMarker[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                payloadStartIndex = i + cceMarker.length;
                break;
            }
        }

        if (payloadStartIndex == -1) {
            LOG.error("Format pesan CCE tidak valid: Separator ,CCE, tidak ditemukan");
            return null;
        }

        CceMessageEnvelope envelope = new CceMessageEnvelope();

        // 4. Ekstraksi Header ASCII
        try {
            String headerAscii = new String(rawData, 0, payloadStartIndex, StandardCharsets.US_ASCII);

            // Format: $$<Ident><Len>,<IMEI>,CCE,
            char idChar = headerAscii.charAt(2);
            envelope.setIdentifier(CceIdentifier.fromChar(idChar));

            String[] headerParts = headerAscii.split(",");
            if (headerParts.length > 1) {
                envelope.setImei(headerParts[1]);
            }
        } catch (Exception e) {
            LOG.error("Gagal mengekstrak Header ASCII CCE", e);
            return null;
        }

        // 5. Ekstraksi Body Biner (Little Endian)
        int binaryLength = asteriskIndex - payloadStartIndex;
        ByteBuffer buffer = ByteBuffer.wrap(rawData, payloadStartIndex, binaryLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        try {
            // Header Biner CCE Universal
            envelope.setCacheRemaining(buffer.getInt());
            int numPackets = Short.toUnsignedInt(buffer.getShort());
            envelope.setNumPackets(numPackets);

            LOG.infof("=== Incoming CCE Packet | Ident: %s | IMEI: %s | Cache: %d | Packets: %d ===",
                    envelope.getIdentifier().name(), envelope.getImei(), envelope.getCacheRemaining(), numPackets);

            // 6. LOOPING SEGMENTASI MULTI-PACKET (Sesuai Manual Hal. 28)
            for (int packetIdx = 0; packetIdx < numPackets; packetIdx++) {
                if (!buffer.hasRemaining()) break;

                int packetLength = Short.toUnsignedInt(buffer.getShort());

                // SABUK PENGAMAN (Defensive Slicing): Ekstrak tepat sesuai packetLength agar korupsi 1 paket tidak menjalar
                int currentPos = buffer.position();
                if (buffer.remaining() < packetLength) {
                    LOG.warnf("Buffer sisa (%d) lebih kecil dari ukuran packetLength yang diklaim (%d). Paket terpotong.", buffer.remaining(), packetLength);
                    break;
                }

                ByteBuffer packetBuffer = buffer.slice();
                packetBuffer.limit(packetLength);
                packetBuffer.order(ByteOrder.LITTLE_ENDIAN);

                // Majukan kursor buffer utama untuk paket berikutnya
                buffer.position(currentPos + packetLength);

                int totalIds = Short.toUnsignedInt(packetBuffer.getShort());

                // --- DECODE 1-BYTE IDs ---
                int num1ByteIds = Byte.toUnsignedInt(packetBuffer.get());
                for (int i = 0; i < num1ByteIds; i++) {
                    decodeParamBlock(packetBuffer, 1, envelope);
                }

                // --- DECODE 2-BYTE IDs ---
                int num2ByteIds = Byte.toUnsignedInt(packetBuffer.get());
                for (int i = 0; i < num2ByteIds; i++) {
                    decodeParamBlock(packetBuffer, 2, envelope);
                }

                // --- DECODE 4-BYTE IDs ---
                int num4ByteIds = Byte.toUnsignedInt(packetBuffer.get());
                for (int i = 0; i < num4ByteIds; i++) {
                    decodeParamBlock(packetBuffer, 4, envelope);
                }

                // --- DECODE UNFIXED-BYTE IDs ---
                int numUnfixedIds = Byte.toUnsignedInt(packetBuffer.get());
                for (int i = 0; i < numUnfixedIds; i++) {
                    decodeParamBlock(packetBuffer, -1, envelope);
                }
            }

        } catch (BufferUnderflowException e) {
            LOG.error("Ukuran payload biner CCE lebih pendek dari yang diinformasikan di header (Corrupted Packet)", e);
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing struktur biner CCE", e);
        }

        return envelope;
    }

    private void decodeParamBlock(ByteBuffer buffer, int dataLength, CceMessageEnvelope envelope) {
        int paramId = Byte.toUnsignedInt(buffer.get());
        boolean isExtended = (paramId == 0xFE || paramId == 0xF8);

        if (isExtended) {
            int nextByte = Byte.toUnsignedInt(buffer.get());
            paramId = (paramId << 8) | nextByte;
        }

        int actualDataLength = dataLength;
        if (dataLength == -1) {
            // Untuk Unfixed, byte berikutnya adalah length (Sesuai Manual Hal. 13-17)
            actualDataLength = Byte.toUnsignedInt(buffer.get());
        }

        int idLen = isExtended ? 2 : 1;
        int blockLen = idLen + (dataLength == -1 ? 1 : 0) + actualDataLength;
        byte[] paramBlock = new byte[blockLen];

        // Rekonstruksi block byte untuk dilempar ke codec (Codec mengharapkan raw utuh termasuk ID & Len)
        int writeIdx = 0;
        if (isExtended) {
            paramBlock[writeIdx++] = (byte) (paramId >> 8);
            paramBlock[writeIdx++] = (byte) (paramId & 0xFF);
        } else {
            paramBlock[writeIdx++] = (byte) paramId;
        }

        if (dataLength == -1) {
            paramBlock[writeIdx++] = (byte) actualDataLength;
        }

        buffer.get(paramBlock, writeIdx, actualDataLength);

        CceMessageCodec<?> decoder = decoderRegistry.get(paramId);
        if (decoder != null) {
            CceDto parsedDto = decoder.decodeBody(paramBlock);
            if (parsedDto != null) {
                envelope.getParameters().add(parsedDto);
            }
        } else {
            LOG.debugf("Belum ada decoder terdaftar untuk Parameter ID: 0x%04X", paramId);
        }
    }
}