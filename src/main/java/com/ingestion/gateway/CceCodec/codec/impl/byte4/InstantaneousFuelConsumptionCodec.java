package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.InstantaneousFuelConsumptionDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InstantaneousFuelConsumptionCodec extends AbstractByte4Codec<InstantaneousFuelConsumptionDto> {

    @Override
    public Class<InstantaneousFuelConsumptionDto> getSupportedDtoClass() {
        return InstantaneousFuelConsumptionDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xa5;
    }

    @Override
    public String getParameterName() {
        return "Instantaneous\n" +
                "fuel consumption\n" +
                "(km/L)";
    }

    @Override
    protected InstantaneousFuelConsumptionDto mapToDto(long decodedValue) {
        InstantaneousFuelConsumptionDto dto = new InstantaneousFuelConsumptionDto();
        dto.setInstantaneousFuelConsumption(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(InstantaneousFuelConsumptionDto dto) {
        return dto.getInstantaneousFuelConsumption();
    }
}