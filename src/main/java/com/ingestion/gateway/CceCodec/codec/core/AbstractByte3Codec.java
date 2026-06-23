package com.ingestion.gateway.CceCodec.codec.core;

import com.ingestion.gateway.CceCodec.codec.CceMessageCodec;
import com.ingestion.gateway.CceCodec.dto.CceDto;
import org.jboss.logging.Logger;

public abstract class AbstractByte3Codec<T extends CceDto> implements CceMessageCodec<T> {

    private static final Logger LOG = Logger.getLogger(AbstractByte3Codec.class);

    @Override
    public int getByteCategory() {
        return 3; // Kategori 3 Byte Khusus
    }

    @Override
    public T decodeBody(byte[] bodyData) {
        // Minimal 4 byte (1 byte ID + 3 byte Nilai)
        if (bodyData == null || bodyData.length < 4) return null;

        int startIndex = (bodyData[0] == (byte) 0xFE || bodyData[0] == (byte) 0xF8) ? 2 : 1;

        if (bodyData.length < startIndex + 3) return null;

        // Potong tepat 3 byte
        byte[] valueBytes = new byte[3];
        System.arraycopy(bodyData, startIndex, valueBytes, 0, 3);

        T dto = mapToDto(valueBytes);

        if (dto != null) {
            dto.setParameterId(getSupportedParameterId());
            LOG.infof("=== Hasil Decode %s ===", getParameterName());
            LOG.infof("Parameter ID : 0x%04X", getSupportedParameterId());
            LOG.infof("Isi DTO      : %s", dto.toString());
        }

        return dto;
    }

    @Override
    public byte[] encodeValue(T dto) {
        // Child class wajib mengembalikan byte[] dengan panjang persis 3
        return mapFromDto(dto);
    }

    protected abstract T mapToDto(byte[] decodedValueBytes);
    protected abstract byte[] mapFromDto(T dto);
}