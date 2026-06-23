package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.TotalEngineRuntimeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TotalEngineRuntimeCodec extends AbstractByte4Codec<TotalEngineRuntimeDto> {

    @Override
    public Class<TotalEngineRuntimeDto> getSupportedDtoClass() {
        return TotalEngineRuntimeDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x9a;
    }

    @Override
    public String getParameterName() {
        return "Total engine run time (h)";
    }

    @Override
    protected TotalEngineRuntimeDto mapToDto(long decodedValue) {
        TotalEngineRuntimeDto dto = new TotalEngineRuntimeDto();
        dto.setTotalEngineRuntime(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(TotalEngineRuntimeDto dto) {
        return dto.getTotalEngineRuntime();
    }
}