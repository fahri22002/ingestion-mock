package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.LongitudeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LongitudeCodec extends AbstractByte4Codec<LongitudeDto> {

    @Override
    public Class<LongitudeDto> getSupportedDtoClass() {
        return LongitudeDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x03;
    }

    @Override
    public String getParameterName() {
        return "Longitude";
    }

    @Override
    protected LongitudeDto mapToDto(long decodedValue) {
        LongitudeDto dto = new LongitudeDto();
        dto.setLongitude(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(LongitudeDto dto) {
        return dto.getLongitude();
    }
}