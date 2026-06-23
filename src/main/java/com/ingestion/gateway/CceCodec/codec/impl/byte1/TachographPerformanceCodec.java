package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.TachographPerformanceDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TachographPerformanceCodec extends AbstractByte1Codec<TachographPerformanceDto> {

    @Override
    public Class<TachographPerformanceDto> getSupportedDtoClass() {
        return TachographPerformanceDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x94;
    }

    @Override
    public String getParameterName() {
        return "Tachograph performance";
    }

    @Override
    protected TachographPerformanceDto mapToDto(short decodedValue) {
        TachographPerformanceDto dto = new TachographPerformanceDto();
        dto.setTachographPerformance(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(TachographPerformanceDto dto) {
        return dto.getTachographPerformance();
    }
}