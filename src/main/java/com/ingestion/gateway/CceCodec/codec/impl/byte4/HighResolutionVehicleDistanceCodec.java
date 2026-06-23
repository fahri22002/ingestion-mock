package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.HighResolutionVehicleDistanceDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HighResolutionVehicleDistanceCodec extends AbstractByte4Codec<HighResolutionVehicleDistanceDto> {

    @Override
    public Class<HighResolutionVehicleDistanceDto> getSupportedDtoClass() {
        return HighResolutionVehicleDistanceDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x9b;
    }

    @Override
    public String getParameterName() {
        return "High resolution vehicle distance (m)";
    }

    @Override
    protected HighResolutionVehicleDistanceDto mapToDto(long decodedValue) {
        HighResolutionVehicleDistanceDto dto = new HighResolutionVehicleDistanceDto();
        dto.setHighResolutionVehicleDistance(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(HighResolutionVehicleDistanceDto dto) {
        return dto.getHighResolutionVehicleDistance();
    }
}