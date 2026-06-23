package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.VehicleBatteryVoltDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VehicleBatteryVoltCodec extends AbstractByte4Codec<VehicleBatteryVoltDto> {

    @Override
    public Class<VehicleBatteryVoltDto> getSupportedDtoClass() {
        return VehicleBatteryVoltDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xf825;
    }

    @Override
    public String getParameterName() {
        return "Vehicle battery voltage";
    }

    @Override
    protected VehicleBatteryVoltDto mapToDto(long decodedValue) {
        VehicleBatteryVoltDto dto = new VehicleBatteryVoltDto();
        dto.setVehicleBatteryVolt(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(VehicleBatteryVoltDto dto) {
        return dto.getVehicleBatteryVolt();
    }
}