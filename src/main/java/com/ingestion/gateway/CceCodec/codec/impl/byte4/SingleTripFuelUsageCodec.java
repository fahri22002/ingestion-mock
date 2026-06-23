package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.SingleTripFuelUsageDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SingleTripFuelUsageCodec extends AbstractByte4Codec<SingleTripFuelUsageDto> {

    @Override
    public Class<SingleTripFuelUsageDto> getSupportedDtoClass() {
        return SingleTripFuelUsageDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xf827;
    }

    @Override
    public String getParameterName() {
        return "Single trip fuel usage";
    }

    @Override
    protected SingleTripFuelUsageDto mapToDto(long decodedValue) {
        SingleTripFuelUsageDto dto = new SingleTripFuelUsageDto();
        dto.setSingleTripFuelUsage(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(SingleTripFuelUsageDto dto) {
        return dto.getSingleTripFuelUsage();
    }
}