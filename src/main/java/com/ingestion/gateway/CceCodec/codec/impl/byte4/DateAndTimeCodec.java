package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.DateAndTimeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DateAndTimeCodec extends AbstractByte4Codec<DateAndTimeDto> {

    @Override
    public Class<DateAndTimeDto> getSupportedDtoClass() {
        return DateAndTimeDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x04;
    }

    @Override
    public String getParameterName() {
        return "Date and time";
    }

    @Override
    protected DateAndTimeDto mapToDto(long decodedValue) {
        DateAndTimeDto dto = new DateAndTimeDto();
        dto.setDateAndTime(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(DateAndTimeDto dto) {
        return dto.getDateAndTime();
    }
}