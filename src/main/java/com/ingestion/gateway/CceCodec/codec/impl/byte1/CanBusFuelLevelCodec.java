package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.CanBusFuelLevelDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CanBusFuelLevelCodec extends AbstractByte1Codec<CanBusFuelLevelDto> {

    @Override
    public Class<CanBusFuelLevelDto> getSupportedDtoClass() {
        return CanBusFuelLevelDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x9d;
    }

    @Override
    public String getParameterName() {
        return "CAN bus fuel level (%)";
    }

    @Override
    protected CanBusFuelLevelDto mapToDto(short decodedValue) {
        CanBusFuelLevelDto dto = new CanBusFuelLevelDto();
        dto.setPersentageCanBusFuelLevel(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(CanBusFuelLevelDto dto) {
        return dto.getPersentageCanBusFuelLevel();
    }
}