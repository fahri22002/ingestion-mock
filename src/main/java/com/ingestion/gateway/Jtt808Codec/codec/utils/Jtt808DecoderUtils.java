package com.ingestion.gateway.Jtt808Codec.codec.utils;

import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Jtt808DecoderUtils {

    private static final Logger LOG = Logger.getLogger(Jtt808DecoderUtils.class);

    // Penyimpanan Memori Statis untuk Segmentasi (Thread-Safe)
    // Struktur: Map<CacheKey, Map<SequenceNumber, ChunkData>>
    private static final Map<String, Map<String, byte[]>> MEMORY_STORAGE = new ConcurrentHashMap<>();

    // Mencegah instansiasi class utilitas
    private Jtt808DecoderUtils() {
        throw new UnsupportedOperationException("Class Utilitas tidak boleh diinstansiasi");
    }

    /**
     * Merakit pecahan paket (Segmentasi JTT808) langsung di RAM.
     */
    public static byte[] processAndAssembleChunks(String imei, int messageId, int totalPackets, int sequence, byte[] chunkData) {
        String cacheKey = String.format("jtt808:segment:%s:%04X", imei, messageId);

        // 1. Siapkan "loker" untuk CacheKey ini jika belum ada
        MEMORY_STORAGE.putIfAbsent(cacheKey, new ConcurrentHashMap<>());
        Map<String, byte[]> allChunks = MEMORY_STORAGE.get(cacheKey);

        // 2. Simpan pecahan ke loker
        allChunks.put(String.valueOf(sequence), chunkData);

        // 3. Evaluasi apakah paket sudah lengkap
        if (allChunks.size() == totalPackets) {
            LOG.infof("✅ Seluruh %d pecahan [%s] diterima di RAM. Merakit ulang...", totalPackets, cacheKey);

            ByteArrayOutputStream assembledPayload = new ByteArrayOutputStream();
            try {
                for (int i = 1; i <= totalPackets; i++) {
                    byte[] part = allChunks.get(String.valueOf(i));
                    if (part != null) {
                        assembledPayload.write(part);
                    }
                }
            } catch (Exception e) {
                LOG.error("Gagal merakit pecahan paket", e);
                return null;
            }

            // PENTING: Hapus dari memori setelah selesai agar tidak terjadi Memory Leak
            MEMORY_STORAGE.remove(cacheKey);
            return assembledPayload.toByteArray();

        } else {
            LOG.infof("⏳ Menunggu pecahan lain di RAM untuk %s... (Terkumpul: %d/%d)", cacheKey, allChunks.size(), totalPackets);
            return null;
        }
    }

    /**
     * Mengembalikan byte 0x7D 0x02 menjadi 0x7E, dan 0x7D 0x01 menjadi 0x7D
     */
    public static byte[] unescape(byte[] data, int start, int length) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = start; i < start + length; i++) {
            if (data[i] == 0x7D && i + 1 < start + length) {
                if (data[i + 1] == 0x02) {
                    baos.write(0x7E);
                    i++;
                } else if (data[i + 1] == 0x01) {
                    baos.write(0x7D);
                    i++;
                } else {
                    baos.write(data[i]);
                }
            } else {
                baos.write(data[i]);
            }
        }
        return baos.toByteArray();
    }

    /**
     * Konversi byte array format BCD menjadi String
     */
    public static String decodeBcd(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString().replaceFirst("^0+(?!$)", "");
    }
}