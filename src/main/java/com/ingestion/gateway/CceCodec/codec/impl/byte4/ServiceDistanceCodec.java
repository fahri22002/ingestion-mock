package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.ServiceDistanceDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceDistanceCodec extends AbstractByte4Codec<ServiceDistanceDto> {

    @Override
    public Class<ServiceDistanceDto> getSupportedDtoClass() {
        return ServiceDistanceDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xa4;
    }

    @Override
    public String getParameterName() {
        return "Service distance (km)";
    }

    @Override
    protected ServiceDistanceDto mapToDto(long decodedValue) {
        ServiceDistanceDto dto = new ServiceDistanceDto();
        dto.setServiceDistance(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(ServiceDistanceDto dto) {
        return dto.getServiceDistance();
    }
}