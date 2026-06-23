package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.SpeedDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SpeedCodec extends AbstractByte2Codec<SpeedDto> {

    @Override
    public Class<SpeedDto> getSupportedDtoClass() {
        return SpeedDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x08;
    }

    @Override
    public String getParameterName() {
        return "Speed";
    }

    @Override
    protected SpeedDto mapToDto(int decodedValue) {
        SpeedDto dto = new SpeedDto();
        dto.setSpeedKmH(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(SpeedDto dto) {
        return dto.getSpeedKmH();
    }
}