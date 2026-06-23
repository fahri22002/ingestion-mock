package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.NewEventCodeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NewEventCodeCodec extends AbstractByte2Codec<NewEventCodeDto> {

    @Override
    public Class<NewEventCodeDto> getSupportedDtoClass() {
        return NewEventCodeDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x40;
    }

    @Override
    public String getParameterName() {
        return "New event code";
    }

    @Override
    protected NewEventCodeDto mapToDto(int decodedValue) {
        NewEventCodeDto dto = new NewEventCodeDto();
        dto.setNewEventCode(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(NewEventCodeDto dto) {
        return dto.getNewEventCode();
    }
}