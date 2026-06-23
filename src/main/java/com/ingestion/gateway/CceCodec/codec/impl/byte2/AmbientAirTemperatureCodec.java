package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.AmbientAirTemperatureDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AmbientAirTemperatureCodec extends AbstractByte2Codec<AmbientAirTemperatureDto> {

    @Override
    public Class<AmbientAirTemperatureDto> getSupportedDtoClass() {
        return AmbientAirTemperatureDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x9f;
    }

    @Override
    public String getParameterName() {
        return "Ambient air temperature (deg C)";
    }

    @Override
    protected AmbientAirTemperatureDto mapToDto(int decodedValue) {
        AmbientAirTemperatureDto dto = new AmbientAirTemperatureDto();
        dto.setDegreeC(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(AmbientAirTemperatureDto dto) {
        return dto.getDegreeC();
    }
}