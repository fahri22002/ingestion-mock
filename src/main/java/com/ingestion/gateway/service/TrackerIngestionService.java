package com.ingestion.gateway.service;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808DecoderDispatcher;
import com.ingestion.gateway.Jtt808Codec.dto.Jtt808MessageEnvelope;
import com.ingestion.gateway.dto.RawTrackerMessageDto;
import com.ingestion.gateway.Jtt808Codec.codec.impl.RealTimeAudioAndVideoStreamCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.RealTimeAudioAndVideoStreamDto;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import jakarta.inject.Inject;

import java.util.concurrent.ConcurrentHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import java.util.function.Consumer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

@ApplicationScoped
public class TrackerIngestionService {

    private static final Logger LOG = Logger.getLogger(TrackerIngestionService.class);

    private Process ffmpegProcess;
    private OutputStream ffmpegStdin;

    @Inject
    DeviceHandshakeService handshakeService;

    @Inject
    DeviceConnectionManager connectionManager;

    // In-memory buffer untuk reassembly JTT1078
    private final ConcurrentHashMap<String, ByteArrayOutputStream> streamBufferMap = new ConcurrentHashMap<>();

    // Direktori tempat menyimpan file video mentah
    private static final String STREAM_OUTPUT_DIR = "mdvr_streams";

    @Inject
    Jtt808DecoderDispatcher jtt808Decoder;

    @Inject
    RealTimeAudioAndVideoStreamCodec streamCodec;

    public void processRawMessage(String remoteAddress, String protocol, byte[] rawBytes, Consumer<byte[]> replier) {
        if (rawBytes == null || rawBytes.length == 0) return;

        LOG.infof("DEBUG: Data masuk! Protocol: %s, Size: %d, First Byte: 0x%02X", protocol, rawBytes.length, rawBytes[0]);
        if (rawBytes[0] == 0x7E) {
            handleJtt808(remoteAddress, rawBytes, replier);
        } else {
            handleJtt1078Stream(remoteAddress, rawBytes);
        }
    }

    private void handleJtt808(String remoteAddress, byte[] rawBytes, Consumer<byte[]> replier) {
        Jtt808MessageEnvelope finalDecodedEnvelope = null;
        try {
            finalDecodedEnvelope = jtt808Decoder.decode(rawBytes);
        } catch (Exception e) {
            LOG.error("Gagal memproses JTT808 dari " + remoteAddress, e);
            return;
        }

        if (finalDecodedEnvelope == null) return;

        int messageId = finalDecodedEnvelope.getMessageId();
        LOG.debugf("Menerima MessageID: 0x%04X dari %s", messageId, remoteAddress);

        // Pola Routing/Dispatcher standar
        switch (messageId) {
            case 0x0100: // Terminal Registration
                byte[] regResponse = handshakeService.processRegistration(finalDecodedEnvelope);
                replier.accept(regResponse); // Kembalikan ke Controller untuk dikirim
                break;

            case 0x0102: // Terminal Authentication
                byte[] authResponse = handshakeService.processAuthentication(finalDecodedEnvelope);
                replier.accept(authResponse);
                connectionManager.registerSession(finalDecodedEnvelope.getImei(), replier);
                LOG.infof("Sesi TCP disimpan untuk IMEI: %s. Perangkat siap menerima perintah.", finalDecodedEnvelope.getImei());
                break;

            case 0x0002: // Terminal Heartbeat
                LOG.debugf("Menerima Heartbeat dari IMEI: %s", finalDecodedEnvelope.getImei());
                // Memanfaatkan metode yang sama karena balasannya sama-sama 0x8001
                byte[] heartbeatResponse = handshakeService.processAuthentication(finalDecodedEnvelope);
                replier.accept(heartbeatResponse);
                break;

            default:
                LOG.debug("Message ID tidak di-handle: " + messageId);
                break;
        }
    }

    private void handleJtt1078Stream(String remoteAddress, byte[] rawBytes) {
        // KITA TIDAK LAGI MENGGUNAKAN CODEC JTT1078 KARENA DATA ADALAH RAW H.264
        // Cukup simpan langsung ke file .h264

        // Gunakan remoteAddress sebagai kunci untuk membedakan stream device
        sendToMediaMtx(rawBytes);

        LOG.debugf("Berhasil menyimpan %d bytes mentah ke file.", rawBytes.length);
    }

    private void processStreamReassembly(String remoteAddress, RealTimeAudioAndVideoStreamDto dto) {
        String streamKey = remoteAddress + "-" + dto.getLogicalChannelNumber();
        int flag = dto.getFragmentationFlag();
        byte[] payload = dto.getDataBody();

        try {
            switch (flag) {
                case 1: // First Packet
                    ByteArrayOutputStream newBuffer = new ByteArrayOutputStream();
                    newBuffer.write(payload);
                    streamBufferMap.put(streamKey, newBuffer);
                    break;

                case 3: // Intermediate Packet
                    ByteArrayOutputStream existingBuffer = streamBufferMap.get(streamKey);
                    if (existingBuffer != null) {
                        existingBuffer.write(payload);
                    }
                    break;

                case 2: // Last Packet
                    ByteArrayOutputStream finalBuffer = streamBufferMap.remove(streamKey);
                    if (finalBuffer != null) {
                        finalBuffer.write(payload);
                        byte[] completeData = finalBuffer.toByteArray();

                        LOG.infof("✅ Stream Selesai Dirakit [%s] | Total: %d bytes", streamKey, completeData.length);

                        // Eksekusi penyimpanan ke file
//                        saveStreamToFile(streamKey, completeData);
                        sendToMediaMtx(completeData);
                    }
                    break;

                case 0: // Single Packet (No Fragmentation)
                    LOG.infof("✅ Single Stream Packet [%s] | Total: %d bytes", streamKey, payload.length);

                    // Eksekusi penyimpanan ke file
                    saveStreamToFile(streamKey, payload);
                    break;
            }
        } catch (IOException e) {
            LOG.error("Gagal merakit stream", e);
            streamBufferMap.remove(streamKey);
        }
    }

    /**
     * Menyimpan raw H.264 payload ke dalam file secara berurutan (append).
     */
    private void saveStreamToFile(String streamKey, byte[] data) {
        try {
            // Bersihkan karakter streamKey agar aman dijadikan nama file (misal IP: 192.168.1.1:5000 -> 192_168_1_1_5000)
            String safeFileName = streamKey.replaceAll("[^a-zA-Z0-9.-]", "_") + ".h264";
            Path path = Paths.get(STREAM_OUTPUT_DIR, safeFileName);

            // Buat folder jika belum ada
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            // Gunakan APPEND agar frame baru ditambahkan ke akhir file video yang sedang berjalan
            Files.write(path, data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            LOG.debugf("Frame disimpan ke file: %s", path.toString());
        } catch (IOException e) {
            LOG.error("Gagal menyimpan stream ke file " + streamKey, e);
        }
    }

    private synchronized void ensureFfmpegRunning() throws IOException {
        if (ffmpegProcess == null || !ffmpegProcess.isAlive()) {
            // PERBAIKI DI SINI: Gunakan path lengkap yang sudah Anda temukan
            String ffmpegPath = "C:\\Users\\User\\Downloads\\ffmpeg-master-latest-win64-gpl-shared\\ffmpeg-master-latest-win64-gpl-shared\\bin\\ffmpeg.exe";
            String rtmpUrl = "rtmp://127.0.0.1/camera1";

            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath, "-f", "h264", "-i", "pipe:0",
                    "-c", "copy", "-f", "flv", rtmpUrl
            );

            // Penting: Redirect error stream agar kita bisa melihat log FFmpeg di console Java
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);

            ffmpegProcess = pb.start();
            ffmpegStdin = ffmpegProcess.getOutputStream();
        }
    }

    private ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();

    private int findStartCode(byte[] data) {
        for (int i = 0; i < data.length - 4; i++) {
            if (data[i] == 0x00 && data[i+1] == 0x00 && data[i+2] == 0x00 && data[i+3] == 0x01) {
                return i;
            }
        }
        return -1;
    }

    private void addToBuffer(byte[] data, int offset) {
        try {
            frameBuffer.write(data, offset, data.length - offset);
        } catch (Exception e) {
            LOG.error("Gagal menulis ke buffer", e);
        }
    }
//
//    private void flushBufferToFfmpeg() {
//        byte[] completeFrame = frameBuffer.toByteArray();
//        if (completeFrame.length > 0) {
//            try {
//                ensureFfmpegRunning();
//                ffmpegStdin.write(completeFrame);
//                ffmpegStdin.flush();
//                LOG.debugf("Frame dikirim ke FFmpeg, size: %d", completeFrame.length);
//            } catch (IOException e) {
//                LOG.error("Gagal menulis ke FFmpeg", e);
//            } finally {
//                frameBuffer.reset(); // Bersihkan buffer setelah dikirim
//            }
//        }
//    }

    private void sendToMediaMtx(byte[] rawBytes) {
        // 1. Deteksi awal frame
        int offset = findStartCode(rawBytes);

        if (offset != -1) {
            // Ditemukan Start Code (00 00 00 01)
            // Kirim frame sebelumnya yang sudah terkumpul di buffer
            if (frameBuffer.size() > 0) {
                flushBufferToFfmpeg();
            }
            // Mulai buffer baru dari offset ini
            addToBuffer(rawBytes, offset);
        } else {
            // Tidak ditemukan Start Code, berarti ini adalah fragmen lanjutan
            // Tambahkan seluruh paket ke buffer
            addToBuffer(rawBytes, 0);
        }
    }

    private void flushBufferToFfmpeg() {
        byte[] completeFrame = frameBuffer.toByteArray();
        if (completeFrame.length > 0) {
            try {
                ensureFfmpegRunning();

                // LOGGING: Print 4 byte pertama untuk validasi Start Code
                // Harusnya hasilnya selalu: 00 00 00 01
                StringBuilder header = new StringBuilder();
                int limit = Math.min(completeFrame.length, 4);
                for (int i = 0; i < limit; i++) {
                    header.append(String.format("%02X ", completeFrame[i]));
                }
                LOG.infof("SENDING TO FFMPEG | Size: %d | Start Code: %s", completeFrame.length, header.toString());

                ffmpegStdin.write(completeFrame);
                ffmpegStdin.flush();
            } catch (IOException e) {
                LOG.error("Gagal menulis ke FFmpeg", e);
            } finally {
                frameBuffer.reset();
            }
        }
    }


}