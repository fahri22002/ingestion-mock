package com.ingestion.gateway.service;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808EncoderDispatcher;
import com.ingestion.gateway.Jtt808Codec.dto.Jtt808MessageEnvelope;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.RealTimeAudioAndVideoTransmissionRequestDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.function.Consumer;

@ApplicationScoped
public class DeviceCommandService {

    private static final Logger LOG = Logger.getLogger(DeviceCommandService.class);

    @Inject
    DeviceConnectionManager connectionManager;

    @Inject
    Jtt808EncoderDispatcher encoderDispatcher;

    /**
     * Memerintahkan MDVR untuk memulai stream video.
     * @return true jika perintah berhasil dikirim, false jika device offline/error.
     */
    public boolean sendStartStreamCommand(String imei, String targetIp, int targetPort, int channel) {
        // 1. Cek Sesi TCP aktif
        Consumer<byte[]> replier = connectionManager.getSession(imei);
        if (replier == null) {
            LOG.warnf("Tidak dapat mengirim 0x9101. IMEI %s sedang offline.", imei);
            return false;
        }

        // 2. Siapkan DTO 0x9101
        RealTimeAudioAndVideoTransmissionRequestDto reqDto = new RealTimeAudioAndVideoTransmissionRequestDto();
        reqDto.setServerIp(targetIp);
        reqDto.setTcpPort(targetPort);
        reqDto.setUdpPort(0);
        reqDto.setLogicalChannelNumber(channel);
        reqDto.setDataType(1); // 1 = Hanya Video (Bisa disesuaikan)
        reqDto.setStreamType(0); // 0 = Primary Stream

        // 3. Bungkus ke dalam Envelope
        Jtt808MessageEnvelope envelope = new Jtt808MessageEnvelope();
        envelope.setMessageId(0x9101);
        envelope.setImei(imei);
        // envelope.setSerialNumber(...) -> Biarkan diatur oleh dispatcher/encoder
        envelope.setProtocolVersion(1);
        envelope.setSegmented(false);
        envelope.setEncryptionMethod(0);
        envelope.setData(reqDto);

        try {
            // 4. Encode & Kirim
            List<byte[]> encodedPackets = encoderDispatcher.encode(envelope);
            if (!encodedPackets.isEmpty()) {
                replier.accept(encodedPackets.get(0));
                LOG.infof("✅ Perintah Buka Kamera %d (0x9101) terkirim ke IMEI: %s", channel, imei);
                return true;
            }
        } catch (Exception e) {
            LOG.error("Gagal men-encode atau mengirim perintah 0x9101 ke " + imei, e);
        }

        return false;
    }
}