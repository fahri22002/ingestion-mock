package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.ActualEngineTorqueDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ActualEngineTorqueCodec extends AbstractByte1Codec<ActualEngineTorqueDto> {

    @Override
    public Class<ActualEngineTorqueDto> getSupportedDtoClass() {
        return ActualEngineTorqueDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x9e;
    }

    @Override
    public String getParameterName() {
        return "Actual engine torque (%)";
    }

    @Override
    protected ActualEngineTorqueDto mapToDto(short decodedValue) {
        ActualEngineTorqueDto dto = new ActualEngineTorqueDto();
        dto.setPersentageActualEngineTorque(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(ActualEngineTorqueDto dto) {
        return dto.getPersentageActualEngineTorque();
    }
}