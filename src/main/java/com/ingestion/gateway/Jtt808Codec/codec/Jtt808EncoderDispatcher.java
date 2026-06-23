package com.ingestion.gateway.Jtt808Codec.codec;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import com.ingestion.gateway.Jtt808Codec.dto.Jtt808MessageEnvelope;
import com.ingestion.gateway.Jtt808Codec.crypto.RsaCryptoService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@ApplicationScoped
public class Jtt808EncoderDispatcher {

    private static final Logger LOG = Logger.getLogger(Jtt808EncoderDispatcher.class);
    private static final int MAX_BODY_LENGTH = 1023;

    @Inject
    Instance<Jtt808MessageCodec<? extends Jtt808Dto>> availableEncoders;

    @Inject
    RsaCryptoService rsaCryptoService;

    private final Map<Integer, Jtt808MessageCodec<Jtt808Dto>> encoderRegistry = new HashMap<>();

    @SuppressWarnings("unchecked")
    void onStart(@Observes StartupEvent ev) {
        for (Jtt808MessageCodec<?> encoder : availableEncoders) {
            encoderRegistry.put(encoder.getSupportedMessageId(), (Jtt808MessageCodec<Jtt808Dto>) encoder);
        }
    }

    @SuppressWarnings("unchecked")
    public void registerEncoderManual(Jtt808MessageCodec<?> encoder) {
        encoderRegistry.put(encoder.getSupportedMessageId(), (Jtt808MessageCodec<Jtt808Dto>) encoder);
    }

    public List<byte[]> encode(Jtt808MessageEnvelope envelope) throws Exception {
        int messageId = envelope.getMessageId();
        Jtt808MessageCodec<Jtt808Dto> encoder = encoderRegistry.get(messageId);
        byte[] fullBodyBytes = new byte[0];

        if (encoder != null && envelope.getData() != null) {
            if (!encoder.getSupportedDtoClass().isInstance(envelope.getData())) {
                LOG.errorf("Mismatched DTO! ID 0x%04X mengharapkan %s", messageId, encoder.getSupportedDtoClass().getSimpleName());
                return new ArrayList<>();
            }
            fullBodyBytes = encoder.encodeValue(envelope.getData());
        }

        if (envelope.getEncryptionMethod() == 1) {
            fullBodyBytes = rsaCryptoService.encrypt(fullBodyBytes, null);
        }

        List<byte[]> encodedPackets = new ArrayList<>();

        // LOGIKA SLICING (SEGMENTASI OTOMATIS)
        if (fullBodyBytes.length <= MAX_BODY_LENGTH && !envelope.isSegmented()) {
            // Data kecil, tidak perlu dipecah
            envelope.setSegmented(false);
            encodedPackets.add(buildSingleFrame(envelope, fullBodyBytes));
        } else {
            // Data raksasa, PECAH menjadi beberapa paket!
            int totalPackets = (int) Math.ceil((double) fullBodyBytes.length / MAX_BODY_LENGTH);
            LOG.infof("Payload sebesar %d byte. Memecah menjadi %d paket...", fullBodyBytes.length, totalPackets);

            envelope.setSegmented(true);
            envelope.setTotalPackets(totalPackets);

            for (int i = 0; i < totalPackets; i++) {
                int offset = i * MAX_BODY_LENGTH;
                int length = Math.min(MAX_BODY_LENGTH, fullBodyBytes.length - offset);

                byte[] chunk = new byte[length];
                System.arraycopy(fullBodyBytes, offset, chunk, 0, length);

                envelope.setPacketSequence(i + 1); // Sequence dimulai dari 1
                encodedPackets.add(buildSingleFrame(envelope, chunk));
            }
        }

        return encodedPackets;
    }

    // Helper: Merakit 1 paket utuh (Header + Body Chunk + Checksum + Escaping)
    private byte[] buildSingleFrame(Jtt808MessageEnvelope env, byte[] bodyChunk) throws Exception {
        boolean hasVersion = env.getProtocolVersion() > 0;
        boolean isSegmented = env.isSegmented();

        int properties = bodyChunk.length & 0x03FF;
        properties |= ((env.getEncryptionMethod() & 0x07) << 10);
        if (isSegmented) properties |= (1 << 13);
        if (hasVersion) properties |= (1 << 14);

        int imeiByteLength = hasVersion ? 10 : 6;
        int headerLen = 4 + (hasVersion ? 1 : 0) + imeiByteLength + 2 + (isSegmented ? 4 : 0);
        ByteBuffer headerBuf = ByteBuffer.allocate(headerLen);

        headerBuf.putShort((short) env.getMessageId());
        headerBuf.putShort((short) properties);
        if (hasVersion) headerBuf.put((byte) env.getProtocolVersion());
        headerBuf.put(encodeBcd(env.getImei(), imeiByteLength));
        headerBuf.putShort((short) env.getSerialNumber());

        if (isSegmented) {
            headerBuf.putShort((short) env.getTotalPackets());
            headerBuf.putShort((short) env.getPacketSequence());
        }

        byte[] headerBytes = headerBuf.array();

        byte checksum = 0;
        for (byte b : headerBytes) checksum ^= b;
        for (byte b : bodyChunk) checksum ^= b;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(0x7E);
        escapeAndWrite(out, headerBytes);
        escapeAndWrite(out, bodyChunk);
        escapeAndWrite(out, new byte[]{checksum});
        out.write(0x7E);

        return out.toByteArray();
    }

    // --- Utility Methods ---

    private void escapeAndWrite(ByteArrayOutputStream stream, byte[] data) {
        for (byte b : data) {
            if (b == 0x7E) {
                stream.write(0x7D);
                stream.write(0x02);
            } else if (b == 0x7D) {
                stream.write(0x7D);
                stream.write(0x01);
            } else {
                stream.write(b);
            }
        }
    }

    private byte[] encodeBcd(String str, int requiredLength) {
        if (str == null) str = "";

        // Padding 0 di depan jika panjang string tidak mencukupi untuk hex (2 digit per byte)
        int requiredStrLen = requiredLength * 2;
        if (str.length() < requiredStrLen) {
            str = String.format("%" + requiredStrLen + "s", str).replace(' ', '0');
        } else if (str.length() > requiredStrLen) {
            str = str.substring(str.length() - requiredStrLen); // Potong dari depan
        }

        byte[] bcd = new byte[requiredLength];
        for (int i = 0; i < requiredLength; i++) {
            bcd[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
        }
        return bcd;
    }
}