package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte2Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.VehicleSpeedWheelDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VehicleSpeedWheelCodec extends AbstractByte2Codec<VehicleSpeedWheelDto> {

    @Override
    public Class<VehicleSpeedWheelDto> getSupportedDtoClass() {
        return VehicleSpeedWheelDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x92;
    }

    @Override
    public String getParameterName() {
        return "Vehicle speed (based on the wheel) (km/h)";
    }

    @Override
    protected VehicleSpeedWheelDto mapToDto(int decodedValue) {
        VehicleSpeedWheelDto dto = new VehicleSpeedWheelDto();
        dto.setSpeedKmHBasedOnWheel(decodedValue);
        return dto;
    }

    @Override
    protected int mapFromDto(VehicleSpeedWheelDto dto) {
        return dto.getSpeedKmHBasedOnWheel();
    }
}