package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.ParkingBrakeSwitchDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ParkingBrakeSwitchCodec extends AbstractByte1Codec<ParkingBrakeSwitchDto> {

    @Override
    public Class<ParkingBrakeSwitchDto> getSupportedDtoClass() {
        return ParkingBrakeSwitchDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x95;
    }

    @Override
    public String getParameterName() {
        return "Parking brake switch";
    }

    @Override
    protected ParkingBrakeSwitchDto mapToDto(short decodedValue) {
        ParkingBrakeSwitchDto dto = new ParkingBrakeSwitchDto();
        dto.setParkingBrakeSwitch(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(ParkingBrakeSwitchDto dto) {
        return dto.getParkingBrakeSwitch();
    }
}