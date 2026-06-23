package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.ActualEngineTorqueAtCurrentSpeedDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ActualEngineTorqueAtCurrentSpeedCodec extends AbstractByte1Codec<ActualEngineTorqueAtCurrentSpeedDto> {

    @Override
    public Class<ActualEngineTorqueAtCurrentSpeedDto> getSupportedDtoClass() {
        return ActualEngineTorqueAtCurrentSpeedDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xa1;
    }

    @Override
    public String getParameterName() {
        return "Actual engine torque at current speed (%)";
    }

    @Override
    protected ActualEngineTorqueAtCurrentSpeedDto mapToDto(short decodedValue) {
        ActualEngineTorqueAtCurrentSpeedDto dto = new ActualEngineTorqueAtCurrentSpeedDto();
        dto.setPersentageActualEngineTorqueAtCurrentSpeed(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(ActualEngineTorqueAtCurrentSpeedDto dto) {
        return dto.getPersentageActualEngineTorqueAtCurrentSpeed();
    }
}