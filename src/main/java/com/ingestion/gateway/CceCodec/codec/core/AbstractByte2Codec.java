package com.ingestion.gateway.CceCodec.codec.core;

import com.ingestion.gateway.CceCodec.codec.CceMessageCodec;
import com.ingestion.gateway.CceCodec.dto.CceDto;
import org.jboss.logging.Logger;

public abstract class AbstractByte2Codec<T extends CceDto> implements CceMessageCodec<T> {

    private static final Logger LOG = Logger.getLogger(AbstractByte2Codec.class);
    @Override
    public int getByteCategory() {
        return 2;
    }

    @Override
    public T decodeBody(byte[] bodyData) {
        if (bodyData == null || bodyData.length < 3) return null;
        int startIndex = (bodyData[0] == (byte) 0xFE || bodyData[0] == (byte) 0xF8) ? 2 : 1;
        if (bodyData.length < startIndex + 2) return null;
        int value = (bodyData[startIndex] & 0xFF) | ((bodyData[startIndex + 1] & 0xFF) << 8);
        T dto = mapToDto(value);

        // Cetak Log
        if (dto != null) {
            dto.setParameterId(getSupportedParameterId());
            LOG.infof("=== Hasil Decode %s ===", getParameterName());
            LOG.infof("Parameter ID : 0x%02X", getSupportedParameterId());
            LOG.infof("Raw Value    : %d (0x%02X)", value, value);
            LOG.infof("Isi DTO      : %s", dto.toString()); // Ini akan mem-print seluruh isi DTO berkat Lombok @Data
        }
        return mapToDto(value);
    }

    @Override
    public byte[] encodeValue(T dto) {
        int value = mapFromDto(dto);
        return new byte[] {
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF)
        };
    }

    protected abstract T mapToDto(int decodedValue);
    protected abstract int mapFromDto(T dto);
}