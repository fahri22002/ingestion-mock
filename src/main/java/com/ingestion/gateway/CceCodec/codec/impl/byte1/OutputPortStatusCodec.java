package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.OutputPortStatusDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OutputPortStatusCodec extends AbstractByte1Codec<OutputPortStatusDto> {

    @Override
    public Class<OutputPortStatusDto> getSupportedDtoClass() {
        return OutputPortStatusDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x14;
    }

    @Override
    public String getParameterName() {
        return "Output port status";
    }

    @Override
    protected OutputPortStatusDto mapToDto(short decodedValue) {
        OutputPortStatusDto dto = new OutputPortStatusDto();
        dto.setOutputPortStatus(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(OutputPortStatusDto dto) {
        return dto.getOutputPortStatus();
    }
}