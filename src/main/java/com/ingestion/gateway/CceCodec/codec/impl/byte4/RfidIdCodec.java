package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.RfidIdDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RfidIdCodec extends AbstractByte4Codec<RfidIdDto> {

    @Override
    public Class<RfidIdDto> getSupportedDtoClass() {
        return RfidIdDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x25;
    }

    @Override
    public String getParameterName() {
        return "RFID ID";
    }

    @Override
    protected RfidIdDto mapToDto(long decodedValue) {
        RfidIdDto dto = new RfidIdDto();
        dto.setRfidId(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(RfidIdDto dto) {
        return dto.getRfidId();
    }
}