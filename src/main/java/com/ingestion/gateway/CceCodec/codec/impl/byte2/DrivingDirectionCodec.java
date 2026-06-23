package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.DrivingDirectionDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DrivingDirectionCodec extends AbstractByte2Codec<DrivingDirectionDto> {

    @Override
    public Class<DrivingDirectionDto> getSupportedDtoClass() {
        return DrivingDirectionDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x09;
    }

    @Override
    public String getParameterName() {
        return "Driving direction";
    }

    @Override
    protected DrivingDirectionDto mapToDto(int decodedValue) {
        DrivingDirectionDto dto = new DrivingDirectionDto();
        dto.setDrivingDirection(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(DrivingDirectionDto dto) {
        return dto.getDrivingDirection();
    }
}