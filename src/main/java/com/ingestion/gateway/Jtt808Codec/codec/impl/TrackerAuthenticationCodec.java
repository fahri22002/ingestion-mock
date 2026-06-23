package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.TrackerAuthenticationDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@ApplicationScoped
public class TrackerAuthenticationCodec implements Jtt808MessageCodec<TrackerAuthenticationDto> {

    private static final Logger LOG = Logger.getLogger(TrackerAuthenticationCodec.class);

    // Protokol menetapkan tipe STRING menggunakan encoding GBK
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x0102; // ID untuk Tracker Authentication
    }

    @Override
    public Class<TrackerAuthenticationDto> getSupportedDtoClass() {
        return TrackerAuthenticationDto.class;
    }

    @Override
    public String getCommandName() {
        return "Tracker Authentication";
    }

    @Override
    public TrackerAuthenticationDto decodeBody(byte[] bodyData) {
        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyData);
        TrackerAuthenticationDto dto = new TrackerAuthenticationDto();

        try {
            // BYTE (1 byte): Authentication Code Length (n)
            int authCodeLength = Byte.toUnsignedInt(bodyBuffer.get());

            // STRING (n bytes): Authentication Code
            byte[] authCodeByte = new byte[authCodeLength];
            bodyBuffer.get(authCodeByte);
            String authCode = new String(authCodeByte, GBK).trim();

            // BYTE[15]: Tracker IMEI
            byte[] trackerImeiByte = new byte[15];
            bodyBuffer.get(trackerImeiByte);
            String trackerImei = new String(trackerImeiByte, GBK).trim();

            // BYTE[20]: Software Version Number
            byte[] softwareVersionNumberByte = new byte[20];
            bodyBuffer.get(softwareVersionNumberByte);
            String softwareVersionNumber = new String(softwareVersionNumberByte, GBK).trim();

            dto.setAuthCodeLength(authCodeLength);
            dto.setAuthCode(authCode);
            dto.setTrackerImei(trackerImei);
            dto.setSoftwareVersionNumber(softwareVersionNumber);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Auth Code Length: %d", authCodeLength);
            LOG.infof("Auth Code       : %s", authCode);
            LOG.infof("Tracker IMEI    : %s", trackerImei);
            LOG.infof("Soft. Ver. Num. : %s", softwareVersionNumber);

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Tracker Authentication", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(TrackerAuthenticationDto dto) {
        try {
            byte[] authCodeBytes = dto.getAuthCode() != null ? dto.getAuthCode().getBytes(GBK) : new byte[0];
            int authCodeLength = authCodeBytes.length;

            // Total Buffer = 1 (Auth Code Length) + n (Auth Code) + 15 (IMEI) + 20 (Software Version)
            ByteBuffer buffer = ByteBuffer.allocate(1 + authCodeLength + 15 + 20);

            // BYTE (1 byte): Auth Code Length
            buffer.put((byte) authCodeLength);

            // STRING (n byte): Auth Code
            buffer.put(authCodeBytes);

            // BYTE[15]: Tracker IMEI
            byte[] imeiBytes = new byte[15];
            if (dto.getTrackerImei() != null) {
                byte[] rawImei = dto.getTrackerImei().getBytes(GBK);
                // Salin maksimal 15 byte, sisa array tetap 0x00
                System.arraycopy(rawImei, 0, imeiBytes, 0, Math.min(rawImei.length, 15));
            }
            buffer.put(imeiBytes);

            // BYTE[20]: Software Version Number (zero-padded if insufficient)
            byte[] versionBytes = new byte[20];
            if (dto.getSoftwareVersionNumber() != null) {
                byte[] rawVersion = dto.getSoftwareVersionNumber().getBytes(GBK);
                // Salin maksimal 20 byte, sisa array tetap terisi 0x00 sebagai padding
                System.arraycopy(rawVersion, 0, versionBytes, 0, Math.min(rawVersion.length, 20));
            }
            buffer.put(versionBytes);

            return buffer.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Tracker Authentication", e);
            return new byte[0];
        }
    }
}