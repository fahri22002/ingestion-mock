package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.EngineCoolantTemperatureDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ExternalPowerCodec extends AbstractByte2Codec<EngineCoolantTemperatureDto> {

    @Override
    public Class<EngineCoolantTemperatureDto> getSupportedDtoClass() {
        return EngineCoolantTemperatureDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x1a;
    }

    @Override
    public String getParameterName() {
        return "External Power";
    }

    @Override
    protected EngineCoolantTemperatureDto mapToDto(int decodedValue) {
        EngineCoolantTemperatureDto dto = new EngineCoolantTemperatureDto();
        dto.setDegreeC(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(EngineCoolantTemperatureDto dto) {
        return dto.getDegreeC();
    }
}