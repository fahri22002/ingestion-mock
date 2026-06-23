package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.AdDto;

public abstract class AdBaseCodec extends AbstractByte2Codec<AdDto> {

    protected abstract int getAdIndex();

    @Override
    public Class<AdDto> getSupportedDtoClass() {
        return AdDto.class;
    }

    @Override
    protected AdDto mapToDto(int decodedValue) {
        AdDto dto = new AdDto();
        dto.setIndex(getAdIndex());
        dto.setAdVolt(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(AdDto dto) {
        return dto.getAdVolt();
    }
}