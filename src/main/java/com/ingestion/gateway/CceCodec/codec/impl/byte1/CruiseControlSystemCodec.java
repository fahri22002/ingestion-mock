package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.CruiseControlSystemDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CruiseControlSystemCodec extends AbstractByte1Codec<CruiseControlSystemDto> {

    @Override
    public Class<CruiseControlSystemDto> getSupportedDtoClass() {
        return CruiseControlSystemDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x96;
    }

    @Override
    public String getParameterName() {
        return "Cruise control system";
    }

    @Override
    protected CruiseControlSystemDto mapToDto(short decodedValue) {
        CruiseControlSystemDto dto = new CruiseControlSystemDto();
        dto.setCruiseControlSystem(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(CruiseControlSystemDto dto) {
        return dto.getCruiseControlSystem();
    }
}