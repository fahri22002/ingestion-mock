package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.VehicleSpeedTachographDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VehicleSpeedTachographCodec extends AbstractByte2Codec<VehicleSpeedTachographDto> {

    @Override
    public Class<VehicleSpeedTachographDto> getSupportedDtoClass() {
        return VehicleSpeedTachographDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x91;
    }

    @Override
    public String getParameterName() {
        return "Vehicle speed (based on the tachograph) (km/h)";
    }

    @Override
    protected VehicleSpeedTachographDto mapToDto(int decodedValue) {
        VehicleSpeedTachographDto dto = new VehicleSpeedTachographDto();
        dto.setSpeedKmHBasedOnTachograph(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(VehicleSpeedTachographDto dto) {
        return dto.getSpeedKmHBasedOnTachograph();
    }
}