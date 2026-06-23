package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.OverSpeedRecoveryEventAssistanceMessageDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OverSpeedRecoveryEventAssistanceMessageCodec extends AbstractByte2Codec<OverSpeedRecoveryEventAssistanceMessageDto> {

    @Override
    public Class<OverSpeedRecoveryEventAssistanceMessageDto> getSupportedDtoClass() {
        return OverSpeedRecoveryEventAssistanceMessageDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xfe30;
    }

    @Override
    public String getParameterName() {
        return "Over-speed Recovery Event Assistance Message";
    }

    @Override
    protected OverSpeedRecoveryEventAssistanceMessageDto mapToDto(int decodedValue) {
        OverSpeedRecoveryEventAssistanceMessageDto dto = new OverSpeedRecoveryEventAssistanceMessageDto();
        dto.setOverSpeedRecoveryEventAssistanceMessage(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(OverSpeedRecoveryEventAssistanceMessageDto dto) {
        return dto.getOverSpeedRecoveryEventAssistanceMessage();
    }
}