package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.PlatformGeneralResponseDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;

@ApplicationScoped
public class PlatformGeneralResponseCodec implements Jtt808MessageCodec<PlatformGeneralResponseDto> {

    private static final Logger LOG = Logger.getLogger(PlatformGeneralResponseCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x8001; // ID untuk Platform General Response
    }

    @Override
    public Class<PlatformGeneralResponseDto> getSupportedDtoClass() {
        return PlatformGeneralResponseDto.class;
    }

    @Override
    public String getCommandName() {
        return "Platform General Response";
    }

    @Override
    public PlatformGeneralResponseDto decodeBody(byte[] bodyData) {
        // ByteBuffer secara default menggunakan Big-Endian yang sesuai dengan standar JTT808
        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyData);
        PlatformGeneralResponseDto dto = new PlatformGeneralResponseDto();

        try {
            // WORD (2 byte): Serial number of the corresponding tracker message
            int responseSerialNumber = Short.toUnsignedInt(bodyBuffer.getShort());

            // WORD (2 byte): Corresponding Tracker Message ID
            int responseId = Short.toUnsignedInt(bodyBuffer.getShort());

            // BYTE (1 byte): Result (0: Success, 1: Failure, 2: Error, 3: Unsupported, 4: Alarm Processing)
            int result = Byte.toUnsignedInt(bodyBuffer.get());

            dto.setResponseSerialNumber(responseSerialNumber);
            dto.setResponseID(responseId);
            dto.setResult(result);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Response Ser Num: %d", responseSerialNumber);
            LOG.infof("Response ID     : %d", responseId);
            LOG.infof("Result          : %d", result);

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Platform General Response", e);
            return null;
        }

        return dto;
    }

    @Override
    public byte[] encodeValue(PlatformGeneralResponseDto dto) {
        try {
            // Total alokasi 5 byte sesuai dokumen: WORD (2) + WORD (2) + BYTE (1)
            ByteBuffer buffer = ByteBuffer.allocate(5);

            // WORD (2 byte)
            buffer.putShort((short) dto.getResponseSerialNumber());

            // WORD (2 byte)
            buffer.putShort((short) dto.getResponseID());

            // BYTE (1 byte)
            buffer.put((byte) dto.getResult());

            return buffer.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Platform General Response", e);
            return new byte[0];
        }
    }
}