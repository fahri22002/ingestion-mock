package com.ingestion.gateway.CceCodec.codec.core;

import com.ingestion.gateway.CceCodec.codec.CceMessageCodec;
import com.ingestion.gateway.CceCodec.dto.CceDto;
import org.jboss.logging.Logger;

public abstract class AbstractByteUnfixedCodec<T extends CceDto> implements CceMessageCodec<T> {

    private static final Logger LOG = Logger.getLogger(AbstractByteUnfixedCodec.class);

    @Override
    public int getByteCategory() {
        return -1; // -1 untuk menandakan Unfixed Byte Category
    }

    @Override
    public T decodeBody(byte[] bodyData) {
        if (bodyData == null || bodyData.length < 2) return null;
        int lengthIndex = (bodyData[0] == (byte) 0xFE || bodyData[0] == (byte) 0xF8) ? 2 : 1;

        if (bodyData.length < lengthIndex + 1) return null;

        int dataLength = bodyData[lengthIndex] & 0xFF;

        int dataStartIndex = lengthIndex + 1;

        if (bodyData.length < dataStartIndex + dataLength) return null;

        byte[] valueBytes = new byte[dataLength];
        System.arraycopy(bodyData, dataStartIndex, valueBytes, 0, dataLength);

        T dto = mapToDto(valueBytes);

        if (dto != null) {
            dto.setParameterId(getSupportedParameterId());
            LOG.infof("=== Hasil Decode %s ===", getParameterName());
            LOG.infof("Parameter ID : 0x%04X", getSupportedParameterId());
            LOG.infof("Data Length  : %d bytes", dataLength);
            LOG.infof("Isi DTO      : %s", dto.toString());
        }

        return dto;
    }

    @Override
    public byte[] encodeValue(T dto) {
        return mapFromDto(dto);
    }

    protected abstract T mapToDto(byte[] decodedValueBytes);
    protected abstract byte[] mapFromDto(T dto);
}