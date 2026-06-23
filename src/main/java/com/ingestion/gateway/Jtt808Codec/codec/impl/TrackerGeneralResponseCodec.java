package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.TrackerGeneralResponseDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;

@ApplicationScoped
public class TrackerGeneralResponseCodec implements Jtt808MessageCodec<TrackerGeneralResponseDto> {

    private static final Logger LOG = Logger.getLogger(TrackerGeneralResponseCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x0001; // ID untuk Tracker General Response
    }

    @Override
    public Class<TrackerGeneralResponseDto> getSupportedDtoClass() {
        return TrackerGeneralResponseDto.class;
    }

    @Override
    public String getCommandName() {
        return "Tracker General Response";
    }

    @Override
    public TrackerGeneralResponseDto decodeBody(byte[] bodyData) {
        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyData);
        TrackerGeneralResponseDto dto = new TrackerGeneralResponseDto();

        try {
            // WORD (2 byte): Serial number of the corresponding platform message
            int responseSerialNumber = Short.toUnsignedInt(bodyBuffer.getShort());

            // WORD (2 byte): ID of the corresponding platform message
            int responseId = Short.toUnsignedInt(bodyBuffer.getShort());

            // BYTE (1 byte): 0: Success/Confirm; 1: Failure; 2: Message Error; 3: Not Supported
            int result = Byte.toUnsignedInt(bodyBuffer.get());

            dto.setResponseSerialNumber(responseSerialNumber);
            dto.setResponseID(responseId);
            dto.setResult(result);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Response Ser Num: %d", responseSerialNumber);
            LOG.infof("Response ID     : %d", responseId);
            LOG.infof("Result          : %d", result); // Diperbaiki format argumen dari %s ke %d

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Tracker General Response", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(TrackerGeneralResponseDto dto) {
        try {
            // Total alokasi tetap 5 byte: WORD (2) + WORD (2) + BYTE (1)
            ByteBuffer buffer = ByteBuffer.allocate(5);

            buffer.putShort((short) dto.getResponseSerialNumber());
            buffer.putShort((short) dto.getResponseID());
            buffer.put((byte) dto.getResult());

            return buffer.array();
        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Tracker General Response", e);
            return new byte[0];
        }
    }
}