package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.FuelLevelDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FuelLevelCodec extends AbstractByte2Codec<FuelLevelDto> {

    @Override
    public Class<FuelLevelDto> getSupportedDtoClass() {
        return FuelLevelDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x29;
    }

    @Override
    public String getParameterName() {
        return "Fuel level (%";
    }

    @Override
    protected FuelLevelDto mapToDto(int decodedValue) {
        FuelLevelDto dto = new FuelLevelDto();
        dto.setFuelLevelPersentage(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(FuelLevelDto dto) {
        return dto.getFuelLevelPersentage();
    }
}