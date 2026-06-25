package com.ingestion.gateway.service;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808EncoderDispatcher;
import com.ingestion.gateway.Jtt808Codec.dto.Jtt808MessageEnvelope;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.TrackerRegistrationResponseDto;
// Asumsi nama DTO untuk 0x8001 Anda adalah GeneralServerResponseDto
 import com.ingestion.gateway.Jtt808Codec.dto.impl.core.PlatformGeneralResponseDto;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class DeviceHandshakeService {

    private static final Logger LOG = Logger.getLogger(DeviceHandshakeService.class);

    // Inject Encoder Dispatcher Anda
    @Inject
    Jtt808EncoderDispatcher encoderDispatcher;

    // Cache sederhana untuk menyimpan AuthCode (Bisa diganti Redis nantinya)
    private final ConcurrentHashMap<String, String> authSessionMap = new ConcurrentHashMap<>();

    /**
     * Membalas pesan 0x0100 dengan 0x8100
     */
    public byte[] processRegistration(Jtt808MessageEnvelope incomingEnvelope) {
        String imei = incomingEnvelope.getImei();
        int incomingSerialNumber = incomingEnvelope.getSerialNumber();
        LOG.infof("Memproses Registrasi (0x0100) untuk IMEI: %s | Serial: %d", imei, incomingSerialNumber);

        // 1. Buat AuthCode statis (Untuk testing)
        String authCode = "AGORA12345";
        authSessionMap.put(imei, authCode);

        // 2. Siapkan DTO Balasan (0x8100)
        TrackerRegistrationResponseDto responseDto = new TrackerRegistrationResponseDto();
        responseDto.setResponseSerialNumber(incomingSerialNumber); // Wajib sama dengan serial pengirim
        responseDto.setResult(0); // 0 = Sukses
        responseDto.setAuthCode(authCode);

        // 3. Bungkus ke dalam Envelope Balasan
        Jtt808MessageEnvelope responseEnvelope = new Jtt808MessageEnvelope();
        responseEnvelope.setMessageId(0x8100);
        responseEnvelope.setImei(imei);
        // Biarkan serialNumber kosong/generate baru jika encoder Anda menangani auto-increment
        // Atau set manual: responseEnvelope.setSerialNumber(getNextSerial());
        responseEnvelope.setProtocolVersion(incomingEnvelope.getProtocolVersion());
        responseEnvelope.setEncryptionMethod(0);
        responseEnvelope.setSegmented(false);
        responseEnvelope.setData(responseDto);

        try {
            // 4. Encode menjadi raw bytes
            List<byte[]> encodedPackets = encoderDispatcher.encode(responseEnvelope);

            // Pesan registrasi ukurannya kecil, pasti tidak terfragmentasi (hanya 1 paket di index 0)
            if (!encodedPackets.isEmpty()) {
                LOG.infof("✅ Berhasil meng-encode 0x8100 untuk IMEI: %s", imei);
                return encodedPackets.get(0);
            }
        } catch (Exception e) {
            LOG.error("Gagal meng-encode balasan 0x8100", e);
        }

        return null;
    }

    /**
     * Membalas pesan 0x0102 dengan 0x8001
     */
    public byte[] processAuthentication(Jtt808MessageEnvelope incomingEnvelope) {
        String imei = incomingEnvelope.getImei();
        int incomingSerialNumber = incomingEnvelope.getSerialNumber();
        LOG.infof("Memproses Autentikasi (0x0102) untuk IMEI: %s | Serial: %d", imei, incomingSerialNumber);

        // --- Opsional: Validasi Auth Code ---
        // Jika Anda sudah memiliki TrackerAuthenticationDto, Anda bisa membuka komentar di bawah ini:
    /*
    TrackerAuthenticationDto authDto = (TrackerAuthenticationDto) incomingEnvelope.getData();
    String receivedAuthCode = authDto.getAuthCode();
    String savedAuthCode = authSessionMap.get(imei);

    int result = 1; // Default gagal
    if (savedAuthCode != null && savedAuthCode.equals(receivedAuthCode)) {
        result = 0; // Sukses
        LOG.infof("Auth Code cocok untuk IMEI: %s", imei);
    } else {
        LOG.warnf("Auth Code TIDAK cocok untuk IMEI: %s", imei);
    }
    */

        // Untuk saat ini, kita *bypass* validasi dan langsung asumsikan Sukses (Result = 0)
        int result = 0;

        // 1. Siapkan DTO Balasan General (0x8001)
        PlatformGeneralResponseDto responseDto = new PlatformGeneralResponseDto();
        responseDto.setResponseSerialNumber(incomingSerialNumber); // Balas ke serial pengirim
        responseDto.setResponseID(0x0102); // Menandakan kita membalas pesan Autentikasi (0x0102)
        responseDto.setResult(result); // 0 = Sukses, 1 = Gagal

        // 2. Bungkus ke dalam Envelope
        Jtt808MessageEnvelope responseEnvelope = new Jtt808MessageEnvelope();
        responseEnvelope.setMessageId(0x8001);
        responseEnvelope.setImei(imei);
        // Biarkan serialNumber di-generate oleh encoder (atau set 0 jika encoder auto-increment)
        responseEnvelope.setProtocolVersion(incomingEnvelope.getProtocolVersion());
        responseEnvelope.setEncryptionMethod(0);
        responseEnvelope.setSegmented(false);
        responseEnvelope.setData(responseDto);

        try {
            // 3. Encode menjadi raw bytes
            List<byte[]> encodedPackets = encoderDispatcher.encode(responseEnvelope);

            if (!encodedPackets.isEmpty()) {
                LOG.infof("✅ Berhasil meng-encode 0x8001 (General Response) untuk IMEI: %s", imei);
                return encodedPackets.get(0); // Pesan 0x8001 pasti muat di 1 paket, tidak ada fragmentasi
            }
        } catch (Exception e) {
            LOG.error("Gagal meng-encode balasan 0x8001", e);
        }

        return null; // Return null jika proses encode gagal, tidak akan ada balasan yang dikirim
    }
}