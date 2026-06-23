package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.TemperatureSensorNoDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TemperatureSensorNoCodec extends AbstractByte1Codec<TemperatureSensorNoDto> {

    @Override
    public Class<TemperatureSensorNoDto> getSupportedDtoClass() {
        return TemperatureSensorNoDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x27;
    }

    @Override
    public String getParameterName() {
        return "Temperature sensor No.";
    }

    @Override
    protected TemperatureSensorNoDto mapToDto(short decodedValue) {
        TemperatureSensorNoDto dto = new TemperatureSensorNoDto();
        dto.setTemperatureSensorNo(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(TemperatureSensorNoDto dto) {
        return dto.getTemperatureSensorNo();
    }
}