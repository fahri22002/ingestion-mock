package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.GeoFenceNumberDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GeoFenceNumberCodec extends AbstractByte1Codec<GeoFenceNumberDto> {

    @Override
    public Class<GeoFenceNumberDto> getSupportedDtoClass() {
        return GeoFenceNumberDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x01;
    }

    @Override
    public String getParameterName() {
        return "GPS Positioning Status";
    }

    @Override
    protected GeoFenceNumberDto mapToDto(short decodedValue) {
        GeoFenceNumberDto dto = new GeoFenceNumberDto();
        dto.setGeoFenceNumber(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(GeoFenceNumberDto dto) {
        return dto.getGeoFenceNumber();
    }
}