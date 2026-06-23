package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.RuntimeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RuntimeCodec extends AbstractByte4Codec<RuntimeDto> {

    @Override
    public Class<RuntimeDto> getSupportedDtoClass() {
        return RuntimeDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x0d;
    }

    @Override
    public String getParameterName() {
        return "Run time";
    }

    @Override
    protected RuntimeDto mapToDto(long decodedValue) {
        RuntimeDto dto = new RuntimeDto();
        dto.setRuntime(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(RuntimeDto dto) {
        return dto.getRuntime();
    }
}