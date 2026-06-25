package com.ingestion.gateway.CceCodec.codec.impl.byte1;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte1Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.InputPortStatusDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InputPortStatusCodec extends AbstractByte1Codec<InputPortStatusDto> {

    @Override
    public Class<InputPortStatusDto> getSupportedDtoClass() {
        return InputPortStatusDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x15;
    }

    @Override
    public String getParameterName() {
        return "Geo-fence number";
    }

    @Override
    protected InputPortStatusDto mapToDto(short decodedValue) {
        InputPortStatusDto dto = new InputPortStatusDto();
        dto.setInputPortStatus(decodedValue);
        return dto;
    }

    @Override
    protected short mapFromDto(InputPortStatusDto dto) {
        return dto.getInputPortStatus();
    }
}