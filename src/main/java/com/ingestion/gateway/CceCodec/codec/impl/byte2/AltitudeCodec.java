package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.AltitudeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AltitudeCodec extends AbstractByte2Codec<AltitudeDto> {

    @Override
    public Class<AltitudeDto> getSupportedDtoClass() {
        return AltitudeDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x0B;
    }

    @Override
    public String getParameterName() {
        return "Altitude";
    }

    @Override
    protected AltitudeDto mapToDto(int decodedValue) {
        AltitudeDto dto = new AltitudeDto();
        dto.setAltitude(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(AltitudeDto dto) {
        return dto.getAltitude();
    }
}