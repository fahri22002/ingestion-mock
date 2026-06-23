package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.HdopDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HdopCodec extends AbstractByte2Codec<HdopDto> {

    @Override
    public Class<HdopDto> getSupportedDtoClass() {
        return HdopDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x0a;
    }

    @Override
    public String getParameterName() {
        return "Horizontal dilution of precision (HDOP)";
    }

    @Override
    protected HdopDto mapToDto(int decodedValue) {
        HdopDto dto = new HdopDto();
        dto.setHdop(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(HdopDto dto) {
        return dto.getHdop();
    }
}