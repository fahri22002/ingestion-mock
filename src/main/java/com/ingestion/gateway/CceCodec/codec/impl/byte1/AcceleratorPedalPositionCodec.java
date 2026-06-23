package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.AcceleratorPedalPositionDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AcceleratorPedalPositionCodec extends AbstractByte1Codec<AcceleratorPedalPositionDto> {

    @Override
    public Class<AcceleratorPedalPositionDto> getSupportedDtoClass() {
        return AcceleratorPedalPositionDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x97;
    }

    @Override
    public String getParameterName() {
        return "Accelerator pedal position (%)";
    }

    @Override
    protected AcceleratorPedalPositionDto mapToDto(short decodedValue) {
        AcceleratorPedalPositionDto dto = new AcceleratorPedalPositionDto();
        dto.setPersentageAcceleratorPedalPosition(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(AcceleratorPedalPositionDto dto) {
        return dto.getPersentageAcceleratorPedalPosition();
    }
}