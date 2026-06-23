package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.TotalFuelConsumptionDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TotalFuelConsumptionCodec extends AbstractByte4Codec<TotalFuelConsumptionDto> {

    @Override
    public Class<TotalFuelConsumptionDto> getSupportedDtoClass() {
        return TotalFuelConsumptionDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x98;
    }

    @Override
    public String getParameterName() {
        return "Total fuel consumption (L)";
    }

    @Override
    protected TotalFuelConsumptionDto mapToDto(long decodedValue) {
        TotalFuelConsumptionDto dto = new TotalFuelConsumptionDto();
        dto.setTotalFuelConsumption(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(TotalFuelConsumptionDto dto) {
        return dto.getTotalFuelConsumption();
    }
}