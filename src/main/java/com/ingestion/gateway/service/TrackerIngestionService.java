package com.ingestion.gateway.service;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808DecoderDispatcher;
import com.ingestion.gateway.Jtt808Codec.dto.Jtt808MessageEnvelope;
import com.ingestion.gateway.dto.RawTrackerMessageDto;
import com.ingestion.gateway.Jtt808Codec.codec.impl.RealTimeAudioAndVideoStreamCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.RealTimeAudioAndVideoStreamDto;
import  com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.ArrayList;
import java.util.Arrays;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

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
    private final ConcurrentHashMap<String, ByteArrayOutputStream> tcpStreamBuffers = new ConcurrentHashMap<>();

    private final LinkedBlockingQueue<byte[]> videoFrameQueue = new LinkedBlockingQueue<>(1000);
    private final ExecutorService ffmpegExecutor = Executors.newSingleThreadExecutor();

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
                if (finalDecodedEnvelope.getData() == null) {
                    LOG.debug("Message ID tidak di-handle: " + messageId);

                }

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(finalDecodedEnvelope.getData());

                    LOG.infof("Isi data : \n%s", jsonString);
                } catch (Exception e) {
                    LOG.error("Gagal convert data ke JSON", e);
                }
                break;
        }
    }

    private void handleJtt1078Stream(String remoteAddress, byte[] rawBytes) {
        // 1. Ambil atau buat buffer untuk koneksi ini
        ByteArrayOutputStream buffer = tcpStreamBuffers.computeIfAbsent(remoteAddress, k -> new ByteArrayOutputStream());

        try {
            // 2. Tambahkan data baru ke ujung buffer
            buffer.write(rawBytes);
            byte[] currentStream = buffer.toByteArray();

            int offset = 0;

            // 3. Looping untuk mencari dan memproses semua frame utuh dalam buffer
            while (offset < currentStream.length - 4) {
                // Cari Magic Word (0x30, 0x31, 0x63, 0x64)
                if (currentStream[offset] == 0x30 && currentStream[offset+1] == 0x31 &&
                        currentStream[offset+2] == 0x63 && currentStream[offset+3] == 0x64) {

                    // Kita menemukan start header. Sekarang kita harus menebak total panjang frame.
                    // Minimal kita butuh 30 bytes untuk membaca parameter di header.
                    if (currentStream.length - offset < 30) {
                        break; // Data belum cukup untuk baca header, tunggu paket TCP berikutnya
                    }

                    // Decode sementara menggunakan potongan dari offset untuk mengetahui panjang body
                    byte[] potentialFrame = Arrays.copyOfRange(currentStream, offset, currentStream.length);
                    RealTimeAudioAndVideoStreamDto streamDto = streamCodec.decodeStream(potentialFrame);

                    if (streamDto != null && streamDto.getDataBody() != null) {
                        int frameLength = streamDto.getTotalFrameLength();

                        // Gunakan INFOF agar terlihat di console
                        LOG.infof("✅ Frame JTT1078 Utuh! Tipe: %d, Fragmen: %d, Size: %d",
                                streamDto.getDataType(), streamDto.getFragmentationFlag(), frameLength);

                        processStreamReassembly(remoteAddress, streamDto);
                        offset += frameLength;
                    } else {
                        // PROTEKSI BARU: Hapus buffer jika ukurannya tidak wajar (corrupt)
                        if (currentStream.length - offset > 70000) {
                            LOG.warn("⚠️ Data melebihi batas JTT1078 namun decode gagal. Asumsi corrupt, skip header ini.");
                            offset++;
                            continue;
                        }
                        break;
                    }
                } else {
                    // Bukan magic word, geser 1 byte ke depan untuk mencari magic word
                    offset++;
                }
            }

            // 4. Bersihkan buffer dari frame yang sudah diproses, simpan sisanya untuk nanti
            if (offset > 0) {
                buffer.reset();
                if (offset < currentStream.length) {
                    buffer.write(currentStream, offset, currentStream.length - offset);
                }
            }

        } catch (Exception e) {
            LOG.error("Gagal memproses stream buffer dari " + remoteAddress, e);
            tcpStreamBuffers.remove(remoteAddress); // Reset jika terjadi fatal error
        }
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
                        // UBAH BARIS INI
                        directSendToFfmpeg(completeData);
                    }
                    break;

                case 0: // Single Packet (No Fragmentation)
                    LOG.debugf("✅ Single Stream Packet [%s] | Total: %d bytes", streamKey, payload.length);
                    // UBAH BARIS INI
                    directSendToFfmpeg(payload);
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

//    private synchronized void ensureFfmpegRunning() throws IOException {
//        if (ffmpegProcess == null || !ffmpegProcess.isAlive()) {
//            // PERBAIKI DI SINI: Gunakan path lengkap yang sudah Anda temukan
//            String ffmpegPath = "C:\\Users\\User\\Downloads\\ffmpeg-master-latest-win64-gpl-shared\\ffmpeg-master-latest-win64-gpl-shared\\bin\\ffmpeg.exe";
//            String rtmpUrl = "rtmp://127.0.0.1/camera1";
//
//            ProcessBuilder pb = new ProcessBuilder(
//                    ffmpegPath, "-f", "h264", "-i", "pipe:0",
//                    "-c", "copy", "-f", "flv", rtmpUrl
//            );
//
//            // Penting: Redirect error stream agar kita bisa melihat log FFmpeg di console Java
//            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
//
//            ffmpegProcess = pb.start();
//            ffmpegStdin = ffmpegProcess.getOutputStream();
//        }
//    }

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
    private synchronized void ensureFfmpegRunning() throws IOException {
        if (ffmpegProcess == null || !ffmpegProcess.isAlive()) {
            String ffmpegPath = "C:\\Users\\User\\Downloads\\ffmpeg-master-latest-win64-gpl-shared\\ffmpeg-master-latest-win64-gpl-shared\\bin\\ffmpeg.exe";
            String rtmpUrl = "rtmp://127.0.0.1/camera1";

            // UBAH KEMBALI PARAMETER FFMPEG KE STANDAR
            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-f", "h264",
                    "-r", "25",
                    "-i", "pipe:0",
                    "-c:v", "copy",
                    "-f", "flv",
                    rtmpUrl
            );

            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);

            ffmpegProcess = pb.start();
            ffmpegStdin = ffmpegProcess.getOutputStream();
            LOG.info("FFmpeg process berhasil distart.");
        }
    }

    private void sendToMediaMtx(byte[] rawBytes) {
        int offset = findStartCode(rawBytes);

        if (offset != -1) {
            // Cek 1 byte setelah Start Code (00 00 00 01) untuk mengetahui Tipe NALU
            if (offset + 4 < rawBytes.length) {
                byte naluByte = rawBytes[offset + 4];
                int naluType = naluByte & 0x1F; // Ambil 5 bit terakhir

                String typeName = "UNKNOWN";
                if (naluType == 7) typeName = "SPS (Sequence Parameter Set) - PENTING!";
                else if (naluType == 8) typeName = "PPS (Picture Parameter Set) - PENTING!";
                else if (naluType == 5) typeName = "I-Frame (IDR)";
                else if (naluType == 1) typeName = "P/B-Frame (Non-IDR)";

                LOG.infof("NALU DETECTED: Type %d [%s]", naluType, typeName);
            }

            if (frameBuffer.size() > 0) {
                flushBufferToFfmpeg();
            }
            addToBuffer(rawBytes, offset);
        } else {
            addToBuffer(rawBytes, 0);
        }
    }

    private void flushBufferToFfmpeg() {
        byte[] completeFrame = frameBuffer.toByteArray();
        if (completeFrame.length > 0) {
            try {
                ensureFfmpegRunning();

                // MEMENUHI PERMINTAAN: Print SEMUA byte (Code per Code) dalam format Hex
                StringBuilder hexDump = new StringBuilder();
                for (byte b : completeFrame) {
                    hexDump.append(String.format("%02X ", b));
                }

                LOG.infof("SENDING TO FFMPEG | Size: %d bytes\n[HEX DUMP MULAI]\n%s\n[HEX DUMP SELESAI]",
                        completeFrame.length, hexDump.toString().trim());

                // Kirim binary ke stdin FFmpeg
                ffmpegStdin.write(completeFrame);
                ffmpegStdin.flush();

            } catch (IOException e) {
                // Tangkap error broken pipe jika FFmpeg tertutup
                LOG.error("Gagal menulis ke FFmpeg. Stream pipe kemungkinan terputus (Broken Pipe).", e);
            } finally {
                frameBuffer.reset();
            }
        }
    }

    @PostConstruct
    public void initFfmpegPusher() {
        ffmpegExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Ambil frame dari antrean (thread akan pasif menunggu jika kosong)
                    byte[] frame = videoFrameQueue.take();

                    ensureFfmpegRunning();

                    // Suntik Start Code (00 00 00 01) jika tidak ada
                    if (!(frame.length >= 4 && frame[0] == 0 && frame[1] == 0 && frame[2] == 0 && frame[3] == 1)) {
                        ffmpegStdin.write(new byte[]{0, 0, 0, 1});
                    }

                    ffmpegStdin.write(frame);
                    ffmpegStdin.flush();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.error("FFmpeg Pusher Thread dihentikan.");
                } catch (Exception e) {
                    LOG.error("Pipa FFmpeg tersumbat atau putus. Mereset proses...", e);
                    if (ffmpegProcess != null) {
                        ffmpegProcess.destroyForcibly();
                    }
                }
            }
        });
    }

    @PreDestroy
    public void cleanup() {
        ffmpegExecutor.shutdownNow();
        if (ffmpegProcess != null) ffmpegProcess.destroyForcibly();
    }

    private void directSendToFfmpeg(byte[] h264Frame) {
        // Tawarkan data ke antrean secara aman tanpa memblokir thread Vert.x
        boolean accepted = videoFrameQueue.offer(h264Frame);
        if (!accepted) {
            LOG.warn("⚠️ Antrean FFmpeg penuh! Frame di-drop untuk mencegah 60000ms timeout Vert.x.");
        }
    }




}