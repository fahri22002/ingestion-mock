package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.InputPortStatusDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InputPortStatusCodec extends AbstractByte4Codec<InputPortStatusDto> {

    @Override
    public Class<InputPortStatusDto> getSupportedDtoClass() {
        return InputPortStatusDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x42;
    }

    @Override
    public String getParameterName() {
        return "Input port status";
    }

    @Override
    protected InputPortStatusDto mapToDto(long decodedValue) {
        InputPortStatusDto dto = new InputPortStatusDto();
        dto.setInputPortStatus(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(InputPortStatusDto dto) {
        return dto.getInputPortStatus();
    }
}