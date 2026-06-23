package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.EventCodeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EventCodeCodec extends AbstractByte1Codec<EventCodeDto> {

    @Override
    public Class<EventCodeDto> getSupportedDtoClass() {
        return EventCodeDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x01;
    }

    @Override
    public String getParameterName() {
        return "Event code";
    }

    @Override
    protected EventCodeDto mapToDto(short decodedValue) {
        EventCodeDto dto = new EventCodeDto();
        dto.setEventCode(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(EventCodeDto dto) {
        return dto.getEventCode();
    }
}