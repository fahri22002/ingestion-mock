package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.GsmSignalStrengthDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GsmSignalStrengthCodec extends AbstractByte1Codec<GsmSignalStrengthDto> {

    @Override
    public Class<GsmSignalStrengthDto> getSupportedDtoClass() {
        return GsmSignalStrengthDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x07;
    }

    @Override
    public String getParameterName() {
        return "GSM signal strength";
    }

    @Override
    protected GsmSignalStrengthDto mapToDto(short decodedValue) {
        GsmSignalStrengthDto dto = new GsmSignalStrengthDto();
        dto.setGsmSignalStrength(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(GsmSignalStrengthDto dto) {
        return dto.getGsmSignalStrength();
    }
}