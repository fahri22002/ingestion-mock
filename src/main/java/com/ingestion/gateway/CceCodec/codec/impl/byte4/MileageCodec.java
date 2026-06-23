package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.MileageDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MileageCodec extends AbstractByte4Codec<MileageDto> {

    @Override
    public Class<MileageDto> getSupportedDtoClass() {
        return MileageDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x0c;
    }

    @Override
    public String getParameterName() {
        return "Mileage";
    }

    @Override
    protected MileageDto mapToDto(long decodedValue) {
        MileageDto dto = new MileageDto();
        dto.setMileage(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(MileageDto dto) {
        return dto.getMileage();
    }
}