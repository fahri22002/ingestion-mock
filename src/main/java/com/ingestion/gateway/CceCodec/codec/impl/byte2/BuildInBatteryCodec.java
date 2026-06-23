package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.BuildInBatteryDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BuildInBatteryCodec extends AbstractByte2Codec<BuildInBatteryDto> {

    @Override
    public Class<BuildInBatteryDto> getSupportedDtoClass() {
        return BuildInBatteryDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x19;
    }

    @Override
    public String getParameterName() {
        return "Built-in battery";
    }

    @Override
    protected BuildInBatteryDto mapToDto(int decodedValue) {
        BuildInBatteryDto dto = new BuildInBatteryDto();
        dto.setRemainingBatteryPower(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(BuildInBatteryDto dto) {
        return dto.getRemainingBatteryPower();
    }
}