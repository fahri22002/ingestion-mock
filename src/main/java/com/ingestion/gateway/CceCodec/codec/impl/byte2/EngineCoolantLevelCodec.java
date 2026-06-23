package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.EngineCoolantLevelDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EngineCoolantLevelCodec extends AbstractByte2Codec<EngineCoolantLevelDto> {

    @Override
    public Class<EngineCoolantLevelDto> getSupportedDtoClass() {
        return EngineCoolantLevelDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xf826;
    }

    @Override
    public String getParameterName() {
        return "Engine coolant level";
    }

    @Override
    protected EngineCoolantLevelDto mapToDto(int decodedValue) {
        EngineCoolantLevelDto dto = new EngineCoolantLevelDto();
        dto.setEngineCoolantLevel(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(EngineCoolantLevelDto dto) {
        return dto.getEngineCoolantLevel();
    }
}