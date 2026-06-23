package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.NumberOfSatelitesDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NumberOfSatellitesCodec extends AbstractByte1Codec<NumberOfSatelitesDto> {

    @Override
    public Class<NumberOfSatelitesDto> getSupportedDtoClass() {
        return NumberOfSatelitesDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x06;
    }

    @Override
    public String getParameterName() {
        return "Number of satellites";
    }

    @Override
    protected NumberOfSatelitesDto mapToDto(short decodedValue) {
        NumberOfSatelitesDto dto = new NumberOfSatelitesDto();
        dto.setNumberOfSatelites(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(NumberOfSatelitesDto dto) {
        return dto.getNumberOfSatelites();
    }
}