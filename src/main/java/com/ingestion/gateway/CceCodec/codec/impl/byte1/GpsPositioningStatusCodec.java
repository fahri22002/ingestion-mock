package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.GpsPositioningStatusDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GpsPositioningStatusCodec extends AbstractByte1Codec<GpsPositioningStatusDto> {

    @Override
    public Class<GpsPositioningStatusDto> getSupportedDtoClass() {
        return GpsPositioningStatusDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x05;
    }

    @Override
    public String getParameterName() {
        return "GPS Positioning Status";
    }

    @Override
    protected GpsPositioningStatusDto mapToDto(short decodedValue) {
        GpsPositioningStatusDto dto = new GpsPositioningStatusDto();
        dto.setGpsPositioningStatus(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(GpsPositioningStatusDto dto) {
        return dto.getGpsPositioningStatus();
    }
}