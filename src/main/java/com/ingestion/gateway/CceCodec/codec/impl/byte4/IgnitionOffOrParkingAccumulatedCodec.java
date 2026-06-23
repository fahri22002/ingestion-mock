package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.IgnitionOffOrParkingAccumulatedTimeDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IgnitionOffOrParkingAccumulatedCodec extends AbstractByte4Codec<IgnitionOffOrParkingAccumulatedTimeDto> {

    @Override
    public Class<IgnitionOffOrParkingAccumulatedTimeDto> getSupportedDtoClass() {
        return IgnitionOffOrParkingAccumulatedTimeDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xfea1;
    }

    @Override
    public String getParameterName() {
        return "Ignition Off or Parking Accumulated Time";
    }

    @Override
    protected IgnitionOffOrParkingAccumulatedTimeDto mapToDto(long decodedValue) {
        IgnitionOffOrParkingAccumulatedTimeDto dto = new IgnitionOffOrParkingAccumulatedTimeDto();
        dto.setIgnitionOffOrParkingAccumulatedTime(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(IgnitionOffOrParkingAccumulatedTimeDto dto) {
        return dto.getIgnitionOffOrParkingAccumulatedTime();
    }
}