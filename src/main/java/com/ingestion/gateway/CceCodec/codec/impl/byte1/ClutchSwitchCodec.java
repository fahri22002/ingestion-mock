package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.ClutchSwitchDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClutchSwitchCodec extends AbstractByte1Codec<ClutchSwitchDto> {

    @Override
    public Class<ClutchSwitchDto> getSupportedDtoClass() {
        return ClutchSwitchDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x93;
    }

    @Override
    public String getParameterName() {
        return "Clutch switch";
    }

    @Override
    protected ClutchSwitchDto mapToDto(short decodedValue) {
        ClutchSwitchDto dto = new ClutchSwitchDto();
        dto.setClutchSwitch(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(ClutchSwitchDto dto) {
        return dto.getClutchSwitch();
    }
}