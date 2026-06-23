package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.SystemFlagDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SystemFlagCodec extends AbstractByte4Codec<SystemFlagDto> {

    @Override
    public Class<SystemFlagDto> getSupportedDtoClass() {
        return SystemFlagDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x1c;
    }

    @Override
    public String getParameterName() {
        return "System flag";
    }

    @Override
    protected SystemFlagDto mapToDto(long decodedValue) {
        SystemFlagDto dto = new SystemFlagDto();
        dto.setSystemFlag(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(SystemFlagDto dto) {
        return dto.getSystemFlag();
    }
}