package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.AxleWeightDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AxleWeightCodec extends AbstractByte4Codec<AxleWeightDto> {

    @Override
    public Class<AxleWeightDto> getSupportedDtoClass() {
        return AxleWeightDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xa3;
    }

    @Override
    public String getParameterName() {
        return "Axle weight (kg)";
    }

    @Override
    protected AxleWeightDto mapToDto(long decodedValue) {
        AxleWeightDto dto = new AxleWeightDto();
        dto.setAxleWeight(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(AxleWeightDto dto) {
        return dto.getAxleWeight();
    }
}