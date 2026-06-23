package com.ingestion.gateway.service;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.hash.HashCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.ingestion.gateway.dto.VideoResourceDto;


import io.vertx.core.Vertx;

@ApplicationScoped
public class Jtt808Decoder {

    private static final Logger LOG = Logger.getLogger(Jtt808Decoder.class);

    @Inject
    RedisDataSource redis;
    public void decode(byte[] rawData) {
        if (rawData[0] != 0x7E || rawData[rawData.length - 1] != 0x7E) {
            LOG.error("Format pesan tidak valid. Tidak diawali/diakhiri dengan 0x7E");
            return;
        }

        byte[] unescapedData = unescape(rawData, 1, rawData.length - 2);
        ByteBuffer buffer = ByteBuffer.wrap(unescapedData);

        try {
            // --- 1. Parsing Message Header ---
            int messageId = Short.toUnsignedInt(buffer.getShort());
            int messageBodyProperty = Short.toUnsignedInt(buffer.getShort());
            int protocolVersion = Byte.toUnsignedInt(buffer.get());

            byte[] imeiBytes = new byte[10];
            buffer.get(imeiBytes);
            String imei = decodeBcd(imeiBytes);

            int messageSerialNumber = Short.toUnsignedInt(buffer.getShort());
            int bodyLength = messageBodyProperty & 0x03FF;

            // Mengecek apakah Bit ke-13 bernilai 1 (0x2000 dalam hex atau 0010 0000 0000 0000 dalam biner)
            boolean isSegmented = (messageBodyProperty & 0x2000) != 0;

            byte[] finalBodyData;
            if (isSegmented) {
                int totalPackets = Short.toUnsignedInt(buffer.getShort());
                int packetSequence = Short.toUnsignedInt(buffer.getShort());

                byte[] chunkBody = new byte[bodyLength];
                buffer.get(chunkBody);

                LOG.infof("[SEGMENT] Menerima pecahan ke-%d dari total %d (IMEI: %s, ID: 0x%04X)", packetSequence, totalPackets, imei, messageId);

                // Kirim ke Redis untuk dirakit
                finalBodyData = processAndAssembleChunks(imei, messageId, totalPackets, packetSequence, chunkBody);

                // Jika finalBodyData masih null, artinya paket belum lengkap. Hentikan eksekusi.
                if (finalBodyData == null) {
                    return;
                }
            } else {
                // Jika tidak tersegmentasi, langsung ambil body utuhnya
                finalBodyData = new byte[bodyLength];
                buffer.get(finalBodyData);
            }

            LOG.infof("=== Hasil Decode Header JTT808 ===");
            LOG.infof("Message ID      : 0x%04X", messageId);
            LOG.infof("Protocol Version: %d", protocolVersion);
            LOG.infof("IMEI            : %s", imei);
            LOG.infof("Serial Number   : %d", messageSerialNumber);
            LOG.infof("Body Length     : %d bytes", bodyLength);


            // Routing parser berdasarkan Message ID
            if (messageId == 0x0100) {
                decodeRegistrationBody(finalBodyData);
            } else if (messageId == 0x1205) {
                decodeVideoResourceListBody(finalBodyData);
            } else {
                LOG.infof("Parser untuk Message ID 0x%04X belum diimplementasikan.", messageId);
            }

            // Validasi sisa byte untuk Checksum (Opsional)
            // 1. Ekstrak Checksum dari pesan yang dikirim
            byte receivedChecksum = buffer.get();

            // 2. Hitung Checksum secara mandiri untuk divalidasi
            byte calculatedChecksum = 0;
            int headerAndBodyLength = unescapedData.length - 1; // Kurangi 1 byte milik checksum itu sendiri

            for (int i = 0; i < headerAndBodyLength; i++) {
                calculatedChecksum ^= unescapedData[i];
            }

            // 3. Bandingkan hasilnya
            if (receivedChecksum == calculatedChecksum) {
                LOG.infof("VALID : (Received: 0x%02X, Calculated: 0x%02X)", receivedChecksum, calculatedChecksum);
            } else {
                LOG.errorf("INVALID : (Received: 0x%02X, Calculated: 0x%02X)", receivedChecksum, calculatedChecksum);
            }

        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing struktur JTT808", e);
        }
    }

    private static void decodeVideoResourceListBody(byte[] bodyData) {
        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyData);
        List<VideoResourceDto> resources = new ArrayList<>();

        try {
            // Membaca Header Body 0x1205 (Total 6 bytes)
            int serialNumber = Short.toUnsignedInt(bodyBuffer.getShort());
            long totalResources = Integer.toUnsignedLong(bodyBuffer.getInt());

            LOG.infof("=== PROSES DECODE 0x1205 ===");
            LOG.infof("Serial Number   : %d", serialNumber);
            LOG.infof("Total Resources : %d", totalResources);

            // Validasi keamanan agar tidak BufferUnderflow
            int entrySize = 28;
            long availableEntries = bodyBuffer.remaining() / entrySize;
            long loopLimit = Math.min(totalResources, availableEntries);

            if (totalResources != availableEntries) {
                LOG.warnf("Peringatan: Total Resources (%d) tidak sama dengan data fisik yang tersedia (%d)", totalResources, availableEntries);
            }

            // Loop untuk memproses setiap entri dalam list
            for (int i = 0; i < loopLimit; i++) {
                int channelNumber = Byte.toUnsignedInt(bodyBuffer.get());

                byte[] startBcd = new byte[6];
                bodyBuffer.get(startBcd);
                String startTime = decodeBcd(startBcd);

                byte[] endBcd = new byte[6];
                bodyBuffer.get(endBcd);
                String endTime = decodeBcd(endBcd);

                long alarmFlag = bodyBuffer.getLong();
                int resourceType = Byte.toUnsignedInt(bodyBuffer.get());
                int streamType = Byte.toUnsignedInt(bodyBuffer.get());
                int storageType = Byte.toUnsignedInt(bodyBuffer.get());
                long fileSize = Integer.toUnsignedLong(bodyBuffer.getInt());

                LOG.infof("Resource [%d] | Channel: %d | Start: %s | End: %s | Size: %d bytes | Alarm: 0x%X",
                        i, channelNumber, startTime, endTime, fileSize, alarmFlag);

                VideoResourceDto dto = new VideoResourceDto();
//                dto.setImei(imei);
                dto.setChannelNumber(channelNumber);
                dto.setFileSize(fileSize);
                resources.add(dto);
            }

            LOG.infof("=== Selesai: %d entri telah di-decode ===", resources.size());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Video Resource List", e);
        }
    }
    /**
     * Menyimpan kepingan ke Redis dan merakitnya jika sudah lengkap
     */
    private byte[] processAndAssembleChunks(String imei, int messageId, int totalPackets, int sequence, byte[] chunkData) {
        // Buat Kunci Unik Redis: jtt808:segment:<IMEI>:<MessageID>
        String cacheKey = String.format("jtt808:segment:%s:%04X", imei, messageId);

        HashCommands<String, String, byte[]> hash = redis.hash(String.class, String.class, byte[].class);

        // 1. Simpan pecahan ini (Field = urutan paket, Value = byte array payload)
        hash.hset(cacheKey, String.valueOf(sequence), chunkData);

        // Set kadaluarsa 5 menit agar memori Redis tidak bocor jika ada paket hilang
        redis.key().expire(cacheKey, 300);

        // 2. Cek apakah semua paket sudah terkumpul
        Map<String, byte[]> allChunks = hash.hgetall(cacheKey);

        if (allChunks.size() == totalPackets) {
            LOG.info("✅ Seluruh " + totalPackets + " pecahan telah diterima. Merakit ulang payload...");

            ByteArrayOutputStream assembledPayload = new ByteArrayOutputStream();
            try {
                // Gabungkan berdasarkan urutan (1, 2, 3, dst)
                for (int i = 1; i <= totalPackets; i++) {
                    byte[] part = allChunks.get(String.valueOf(i));
                    if (part != null) {
                        assembledPayload.write(part);
                    }
                }
            } catch (Exception e) {
                LOG.error("Gagal merakit pecahan", e);
            }

            // Bersihkan Redis karena data sudah berhasil dirakit
            redis.key().del(cacheKey);

            return assembledPayload.toByteArray();
        } else {
            LOG.infof("⏳ Menunggu pecahan lain... (Terkumpul: %d/%d)", allChunks.size(), totalPackets);
            return null;
        }
    }

    /**
     * Menerjemahkan spesifik Message Body untuk Tracker Registration (0x0100)
     */
    private static void decodeRegistrationBody(byte[] bodyData) {
        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyData);

        try {
            int provinceId = Short.toUnsignedInt(bodyBuffer.getShort()); // WORD (2 byte)
            int cityId = Short.toUnsignedInt(bodyBuffer.getShort());     // WORD (2 byte)

            byte[] manufacturerIdBytes = new byte[5];
            bodyBuffer.get(manufacturerIdBytes);
            String manufacturerId = new String(manufacturerIdBytes, StandardCharsets.UTF_8).trim();

            byte[] terminalModelBytes = new byte[20];
            bodyBuffer.get(terminalModelBytes);
            String terminalModel = new String(terminalModelBytes, StandardCharsets.UTF_8).trim();

            byte[] terminalIdBytes = new byte[7];
            bodyBuffer.get(terminalIdBytes);
            String terminalId = new String(terminalIdBytes, StandardCharsets.UTF_8).trim();

            int plateColor = Byte.toUnsignedInt(bodyBuffer.get()); // BYTE (1 byte)

            // Sisa byte adalah plat nomor kendaraan (STRING)
            byte[] vehicleIdBytes = new byte[bodyBuffer.remaining()];
            bodyBuffer.get(vehicleIdBytes);
            String vehicleId = new String(vehicleIdBytes, StandardCharsets.UTF_8).trim();

            LOG.infof("=== Hasil Decode Body (0x0100 - Tracker Registration) ===");
            LOG.infof("Province ID     : %d", provinceId);
            LOG.infof("City/County ID  : %d", cityId);
            LOG.infof("Manufacturer ID : %s", manufacturerId);
            LOG.infof("Terminal Model  : %s", terminalModel);
            LOG.infof("Terminal ID     : %s", terminalId);
            LOG.infof("Plate Color     : %d", plateColor);
            LOG.infof("Vehicle ID      : %s", vehicleId);

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body registrasi", e);
        }
    }

    private static byte[] unescape(byte[] data, int start, int length) {
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

    private static String decodeBcd(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString().replaceFirst("^0+(?!$)", "");
    }
}