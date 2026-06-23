package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.DrivingStateDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DrivingStateCodec extends AbstractByte1Codec<DrivingStateDto> {

    @Override
    public Class<DrivingStateDto> getSupportedDtoClass() {
        return DrivingStateDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xFEA0;
    }

    @Override
    public String getParameterName() {
        return "driving state";
    }

    @Override
    protected DrivingStateDto mapToDto(short decodedValue) {
        DrivingStateDto dto = new DrivingStateDto();
        dto.setDrivingState(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(DrivingStateDto dto) {
        return dto.getDrivingState();
    }
}