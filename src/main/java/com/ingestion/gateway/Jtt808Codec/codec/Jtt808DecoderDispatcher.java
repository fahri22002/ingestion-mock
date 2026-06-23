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
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class Jtt808DecoderDispatcher {

    private static final Logger LOG = Logger.getLogger(Jtt808DecoderDispatcher.class);

    @Inject
    Instance<Jtt808MessageCodec<?>> availableDecoders;

    @Inject
    RsaCryptoService rsaCryptoService;

    private final Map<Integer, Jtt808MessageCodec<?>> decoderRegistry = new HashMap<>();
    // TEMPAT PENAMPUNGAN PAKET SEGMENTASI
    // Key: "IMEI-MessageID", Value: Map<SequenceNumber, byte[] BodyChunk>
    // Catatan: Jika deploy Multi-Node (Kubernetes), gunakan Redis (Map<String, byte[]>) alih-alih ConcurrentHashMap
    private final Map<String, Map<Integer, byte[]>> aggregationCache = new ConcurrentHashMap<>();

    void onStart(@Observes StartupEvent ev) {
        for (Jtt808MessageCodec<?> decoder : availableDecoders) {
            decoderRegistry.put(decoder.getSupportedMessageId(), decoder);
            LOG.infof("Registered JTT808 Decoder untuk Message ID: 0x%04X (%s)",
                    decoder.getSupportedMessageId(), decoder.getCommandName());
        }
    }

    public Jtt808MessageEnvelope decode(byte[] rawData) {
        byte[] unescaped = unescape(rawData);
        if (unescaped == null || unescaped.length < 12) return null;
        if (!verifyChecksum(unescaped)) return null;

        ByteBuffer buffer = ByteBuffer.wrap(unescaped);
        Jtt808MessageEnvelope envelope = new Jtt808MessageEnvelope();

        try {
            int messageId = Short.toUnsignedInt(buffer.getShort());
            envelope.setMessageId(messageId);

            int properties = Short.toUnsignedInt(buffer.getShort());
            int bodyLength = properties & 0x03FF;
            int encryptionMethod = (properties >> 10) & 0x07;
            boolean isSegmented = ((properties >> 13) & 0x01) == 1;
            boolean hasVersion = ((properties >> 14) & 0x01) == 1;

            envelope.setEncryptionMethod(encryptionMethod);
            envelope.setSegmented(isSegmented);

            byte[] imeiBytes = new byte[hasVersion ? 10 : 6];
            if (hasVersion) envelope.setProtocolVersion(Byte.toUnsignedInt(buffer.get()));
            buffer.get(imeiBytes);
            envelope.setImei(decodeBcd(imeiBytes));

            envelope.setSerialNumber(Short.toUnsignedInt(buffer.getShort()));

            if (isSegmented) {
                envelope.setTotalPackets(Short.toUnsignedInt(buffer.getShort()));
                envelope.setPacketSequence(Short.toUnsignedInt(buffer.getShort()));
            }

            byte[] bodyBytes = new byte[bodyLength];
            buffer.get(bodyBytes);

            if (encryptionMethod == 1) {
                bodyBytes = rsaCryptoService.decrypt(bodyBytes);
            }

            // ================= LOGIKA AGREGASI =================
            if (isSegmented) {
                String cacheKey = envelope.getImei() + "-" + String.format("0x%04X", messageId);
                aggregationCache.putIfAbsent(cacheKey, new ConcurrentHashMap<>());
                Map<Integer, byte[]> chunks = aggregationCache.get(cacheKey);

                // Simpan potongan body ke dalam memori
                chunks.put(envelope.getPacketSequence(), bodyBytes);

                LOG.infof("Menerima paket segmen %d dari %d untuk %s",
                        envelope.getPacketSequence(), envelope.getTotalPackets(), cacheKey);

                // Cek apakah semua paket sudah terkumpul?
                if (chunks.size() == envelope.getTotalPackets()) {
                    LOG.infof("Semua %d segmen untuk %s telah diterima. Menggabungkan...", envelope.getTotalPackets(), cacheKey);

                    // Hitung total ukuran seluruh array
                    int totalSize = chunks.values().stream().mapToInt(b -> b.length).sum();
                    byte[] unifiedBody = new byte[totalSize];
                    int destPos = 0;

                    // Menggabungkan array secara berurutan sesuai sequence
                    for (int i = 1; i <= envelope.getTotalPackets(); i++) {
                        byte[] chunk = chunks.get(i);
                        System.arraycopy(chunk, 0, unifiedBody, destPos, chunk.length);
                        destPos += chunk.length;
                    }

                    // Bersihkan cache dan ganti bodyBytes dengan versi yang utuh
                    aggregationCache.remove(cacheKey);
                    bodyBytes = unifiedBody;

                } else {
                    // Paket belum lengkap, kembalikan envelope tanpa DTO
                    LOG.debug("Menunggu segmen lainnya...");
                    return envelope;
                }
            }
            // ====================================================

            // Data sudah pasti utuh (baik pesan tunggal maupun hasil gabungan), kirim ke Codec
            dispatchToDecoder(messageId, bodyBytes, envelope);

        } catch (Exception e) {
            LOG.error("Gagal parsing header JTT808", e);
        }

        return envelope;
    }

    private void dispatchToDecoder(int messageId, byte[] bodyBytes, Jtt808MessageEnvelope envelope) {
        Jtt808MessageCodec<?> decoder = decoderRegistry.get(messageId);
        if (decoder != null) {
            envelope.setData(decoder.decodeBody(bodyBytes));
        }
    }

    // --- Utility Methods ---

    private byte[] unescape(byte[] raw) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int start = (raw[0] == 0x7E) ? 1 : 0;
        int end = (raw[raw.length - 1] == 0x7E) ? raw.length - 1 : raw.length;

        for (int i = start; i < end; i++) {
            if (raw[i] == 0x7D && i + 1 < end) {
                if (raw[i + 1] == 0x02) {
                    out.write(0x7E);
                    i++;
                } else if (raw[i + 1] == 0x01) {
                    out.write(0x7D);
                    i++;
                } else {
                    out.write(raw[i]);
                }
            } else {
                out.write(raw[i]);
            }
        }
        return out.toByteArray();
    }

    private boolean verifyChecksum(byte[] unescaped) {
        if (unescaped.length < 2) return false;
        byte checksum = 0;
        for (int i = 0; i < unescaped.length - 1; i++) {
            checksum ^= unescaped[i];
        }
        return checksum == unescaped[unescaped.length - 1];
    }

    private String decodeBcd(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        String result = sb.toString();

        // Mempertahankan struktur BCD utuh sesuai diskusi panjang sebelumnya
        // untuk mencegah terhapusnya angka '0' depan yang krusial pada IMEI.
        int firstNonZero = 0;
        while (firstNonZero < result.length() - 1 && result.charAt(firstNonZero) == '0') {
            firstNonZero++;
        }

        String stripped = result.substring(firstNonZero);

        // Fallback untuk IMEI (15 digit) atau Nomor SIM (11/12 digit)
        if (stripped.length() < 15 && result.length() >= 15) {
            return result.substring(result.length() - 15);
        }
        return stripped;
    }
}