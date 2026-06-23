package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.DeadReckoningStateDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeadReckoningStateCodec extends AbstractByte1Codec<DeadReckoningStateDto> {

    @Override
    public Class<DeadReckoningStateDto> getSupportedDtoClass() {
        return DeadReckoningStateDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x5b;
    }

    @Override
    public String getParameterName() {
        return "Dead Reckoning state";
    }

    @Override
    protected DeadReckoningStateDto mapToDto(short decodedValue) {
        DeadReckoningStateDto dto = new DeadReckoningStateDto();
        dto.setDeadReckoningState(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(DeadReckoningStateDto dto) {
        return dto.getDeadReckoningState();
    }
}