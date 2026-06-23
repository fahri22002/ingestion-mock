package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.LatitudeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LatitudeCodec extends AbstractByte4Codec<LatitudeDto> {

    @Override
    public Class<LatitudeDto> getSupportedDtoClass() {
        return LatitudeDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x02;
    }

    @Override
    public String getParameterName() {
        return "Latitude";
    }

    @Override
    protected LatitudeDto mapToDto(long decodedValue) {
        LatitudeDto dto = new LatitudeDto();
        dto.setLatitude(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(LatitudeDto dto) {
        return dto.getLatitude();
    }
}