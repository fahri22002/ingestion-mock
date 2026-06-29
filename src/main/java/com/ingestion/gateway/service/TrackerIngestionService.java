package com.ingestion.gateway.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingestion.gateway.Jtt808Codec.codec.Jtt808DecoderDispatcher;
import com.ingestion.gateway.Jtt808Codec.codec.impl.RealTimeAudioAndVideoStreamCodec;
import com.ingestion.gateway.Jtt808Codec.dto.Jtt808MessageEnvelope;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.RealTimeAudioAndVideoStreamDto;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@ApplicationScoped
public class TrackerIngestionService {

    private static final Logger LOG = Logger.getLogger(TrackerIngestionService.class);

    private Process ffmpegProcess;
    private OutputStream ffmpegStdin;

    @Inject
    DeviceHandshakeService handshakeService;

    @Inject
    DeviceConnectionManager connectionManager;

    @Inject
    Jtt808DecoderDispatcher jtt808Decoder;

    @Inject
    RealTimeAudioAndVideoStreamCodec streamCodec;

    // Buffer management
    private final ConcurrentHashMap<String, ByteArrayOutputStream> streamBufferMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ByteArrayOutputStream> tcpStreamBuffers = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<byte[]> videoFrameQueue = new LinkedBlockingQueue<>(1000);
    private final ExecutorService ffmpegExecutor = Executors.newSingleThreadExecutor();

    private static final String STREAM_OUTPUT_DIR = "mdvr_streams";
    private boolean isFirstIFrameReceived = false;

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

        switch (messageId) {
            case 0x0100: // Terminal Registration
                byte[] regResponse = handshakeService.processRegistration(finalDecodedEnvelope);
                replier.accept(regResponse);
                break;

            case 0x0102: // Terminal Authentication
                byte[] authResponse = handshakeService.processAuthentication(finalDecodedEnvelope);
                replier.accept(authResponse);
                connectionManager.registerSession(finalDecodedEnvelope.getImei(), replier);
                LOG.infof("Sesi TCP disimpan untuk IMEI: %s. Perangkat siap menerima perintah.", finalDecodedEnvelope.getImei());
                break;

            case 0x0002: // Terminal Heartbeat
                LOG.debugf("Menerima Heartbeat dari IMEI: %s", finalDecodedEnvelope.getImei());
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
        try {
            // rawBytes dari Controller sekarang dijamin 100% merupakan 1 frame utuh
            RealTimeAudioAndVideoStreamDto streamDto = streamCodec.decodeStream(rawBytes);

            if (streamDto != null && streamDto.getDataBody() != null) {
                // Hapus INFOF jika log terminal Anda terlalu penuh
                // LOG.infof("✅ Frame JTT1078 Utuh! Tipe: %d, Fragmen: %d, Size: %d",
                //        streamDto.getDataType(), streamDto.getFragmentationFlag(), streamDto.getTotalFrameLength());

                processStreamReassembly(remoteAddress, streamDto);
            } else {
                LOG.warn("⚠️ Menerima frame yang tidak dapat di-decode.");
            }
        } catch (Exception e) {
            LOG.error("Gagal memproses stream dari " + remoteAddress, e);
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
                        // PERBAIKAN: Langsung passing 'dto' parameter bawaan fungsi ini, tidak perlu decode ulang.
                        directSendToFfmpeg(dto, completeData);
                    }
                    break;

                case 0: // Single Packet (No Fragmentation)
                    LOG.debugf("✅ Single Stream Packet [%s] | Total: %d bytes", streamKey, payload.length);
                    // PERBAIKAN: Langsung passing 'dto' parameter bawaan fungsi ini.
                    directSendToFfmpeg(dto, payload);
                    break;
            }
        } catch (IOException e) {
            LOG.error("Gagal merakit stream", e);
            streamBufferMap.remove(streamKey);
        }
    }

    private void directSendToFfmpeg(RealTimeAudioAndVideoStreamDto dto, byte[] h264Frame) {
        if (!isFirstIFrameReceived) {
            // Tipe 0 adalah I-Frame (SPS/PPS/IDR)
            if (dto.getDataType() == 0) {
                isFirstIFrameReceived = true; // Kunci dibuka!
                LOG.info("✅ I-Frame Pertama diterima! Membuka aliran video ke FFmpeg...");
                videoFrameQueue.offer(h264Frame);
            } else {
                LOG.debug("Mengabaikan P-Frame (Tipe " + dto.getDataType() + ") karena menunggu I-Frame pertama...");
            }
        } else {
            videoFrameQueue.offer(h264Frame);
        }
    }

    private synchronized void ensureFfmpegRunning() throws IOException {
        if (ffmpegProcess == null || !ffmpegProcess.isAlive()) {
            String ffmpegPath = "C:\\Users\\User\\Downloads\\ffmpeg-master-latest-win64-gpl-shared\\ffmpeg-master-latest-win64-gpl-shared\\bin\\ffmpeg.exe";
            String rtmpUrl = "rtmp://127.0.0.1/camera1";

            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-loglevel", "error",
                    // --- TAMBAHAN DUA BARIS INI SANGAT KRUSIAL ---
                    "-use_wallclock_as_timestamps", "1", // Gunakan waktu real-time sebagai acuan waktu video
                    "-fflags", "+genpts",                // Minta FFmpeg men-generate PTS/DTS yang kosong
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

    @PostConstruct
    public void initFfmpegPusher() {
        ffmpegExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] frame = videoFrameQueue.take();
                    ensureFfmpegRunning();

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
}