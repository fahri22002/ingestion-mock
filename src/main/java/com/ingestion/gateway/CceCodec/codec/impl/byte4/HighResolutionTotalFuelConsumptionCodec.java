package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.HighResolutionTotalFuelConsumptionDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HighResolutionTotalFuelConsumptionCodec extends AbstractByte4Codec<HighResolutionTotalFuelConsumptionDto> {

    @Override
    public Class<HighResolutionTotalFuelConsumptionDto> getSupportedDtoClass() {
        return HighResolutionTotalFuelConsumptionDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xa0;
    }

    @Override
    public String getParameterName() {
        return "High resolution total fuel consumption (L)";
    }

    @Override
    protected HighResolutionTotalFuelConsumptionDto mapToDto(long decodedValue) {
        HighResolutionTotalFuelConsumptionDto dto = new HighResolutionTotalFuelConsumptionDto();
        dto.setHighResolutionTotalFuelConsumption(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(HighResolutionTotalFuelConsumptionDto dto) {
        return dto.getHighResolutionTotalFuelConsumption();
    }
}