package com.ingestion.gateway.CceCodec.codec.core;

import com.ingestion.gateway.CceCodec.codec.CceMessageCodec;
import com.ingestion.gateway.CceCodec.dto.CceDto;
import org.jboss.logging.Logger;

public abstract class AbstractByte4Codec<T extends CceDto> implements CceMessageCodec<T> {

    private static final Logger LOG = Logger.getLogger(AbstractByte4Codec.class);

    @Override
    public int getByteCategory() {
        return 4; // Kategori 4 Byte
    }

    @Override
    public T decodeBody(byte[] bodyData) {
        if (bodyData == null || bodyData.length < 5) return null;

        int startIndex = (bodyData[0] == (byte) 0xFE || bodyData[0] == (byte) 0xF8) ? 2 : 1;

        if (bodyData.length < startIndex + 4) return null;

        long value = (bodyData[startIndex] & 0xFFL) |
                ((bodyData[startIndex + 1] & 0xFFL) << 8) |
                ((bodyData[startIndex + 2] & 0xFFL) << 16) |
                ((bodyData[startIndex + 3] & 0xFFL) << 24);

        T dto = mapToDto(value);

        if (dto != null) {
            dto.setParameterId(getSupportedParameterId());
            LOG.infof("=== Hasil Decode %s ===", getParameterName());
            LOG.infof("Parameter ID : 0x%04X", getSupportedParameterId());
            LOG.infof("Raw Value    : %d (0x%08X)", value, value);
            LOG.infof("Isi DTO      : %s", dto.toString());
        }

        return dto;
    }

    @Override
    public byte[] encodeValue(T dto) {
        long value = mapFromDto(dto);

        return new byte[] {
                (byte) (value & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 24) & 0xFF)
        };
    }

    protected abstract T mapToDto(long decodedValue);
    protected abstract long mapFromDto(T dto);
}