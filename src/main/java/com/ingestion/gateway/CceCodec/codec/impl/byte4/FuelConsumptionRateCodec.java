package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.FuelConsumptionRateDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FuelConsumptionRateCodec extends AbstractByte4Codec<FuelConsumptionRateDto> {

    @Override
    public Class<FuelConsumptionRateDto> getSupportedDtoClass() {
        return FuelConsumptionRateDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xa2;
    }

    @Override
    public String getParameterName() {
        return "Fuel consumption rate (L/H)";
    }

    @Override
    protected FuelConsumptionRateDto mapToDto(long decodedValue) {
        FuelConsumptionRateDto dto = new FuelConsumptionRateDto();
        dto.setFuelConsumptionRate(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(FuelConsumptionRateDto dto) {
        return dto.getFuelConsumptionRate();
    }
}