package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.TrackerRegistrationResponseDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@ApplicationScoped
public class TrackerRegistrationResponseCodec implements Jtt808MessageCodec<TrackerRegistrationResponseDto> {

    private static final Logger LOG = Logger.getLogger(TrackerRegistrationResponseCodec.class);

    // Standar encoding protokol untuk STRING adalah GBK
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x8100; // ID untuk Tracker Registration Response
    }

    @Override
    public Class<TrackerRegistrationResponseDto> getSupportedDtoClass() {
        return TrackerRegistrationResponseDto.class;
    }

    @Override
    public String getCommandName() {
        return "Tracker Registration Response";
    }

    @Override
    public TrackerRegistrationResponseDto decodeBody(byte[] bodyData) {
        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyData);
        TrackerRegistrationResponseDto dto = new TrackerRegistrationResponseDto();

        try {
            // WORD (2 byte): Serial number corresponding to the tracker registration message
            int responseSerialNumber = Short.toUnsignedInt(bodyBuffer.getShort());

            // BYTE (1 byte): Result (0: Success, 1-4: Various error states)
            int result = Byte.toUnsignedInt(bodyBuffer.get());

            String authCode = "";

            // Auth Code HANYA ada jika result == 0 (Success) dan masih ada sisa byte di buffer
            if (result == 0 && bodyBuffer.hasRemaining()) {
                byte[] authBytes = new byte[bodyBuffer.remaining()];
                bodyBuffer.get(authBytes); // Ambil semua sisa byte
                authCode = new String(authBytes, GBK).trim(); // Dekode menggunakan GBK
            }

            dto.setResponseSerialNumber(responseSerialNumber);
            dto.setResult(result);
            dto.setAuthCode(authCode);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Response Ser Num: %d", responseSerialNumber);
            LOG.infof("Result          : %d", result);
            LOG.infof("Auth Code       : %s", authCode);

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Tracker Registration Response", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(TrackerRegistrationResponseDto dto) {
        try {
            byte[] authCodeBytes = new byte[0];

            // Peraturan: string Auth Code hanya di-encode jika result sukses (0)
            if (dto.getResult() == 0 && dto.getAuthCode() != null && !dto.getAuthCode().isEmpty()) {
                authCodeBytes = dto.getAuthCode().getBytes(GBK);
            }

            // Total buffer: 2 (WORD) + 1 (BYTE) + panjang authCode (jika ada)
            ByteBuffer buffer = ByteBuffer.allocate(3 + authCodeBytes.length);

            // WORD (2 byte)
            buffer.putShort((short) dto.getResponseSerialNumber());

            // BYTE (1 byte)
            buffer.put((byte) dto.getResult());

            // STRING (opsional)
            if (authCodeBytes.length > 0) {
                buffer.put(authCodeBytes);
            }

            return buffer.array();
        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Tracker Registration Response", e);
            return new byte[0];
        }
    }
}