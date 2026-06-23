package com.ingestion.gateway.CceCodec.codec.core;

import com.ingestion.gateway.CceCodec.codec.CceMessageCodec;
import com.ingestion.gateway.CceCodec.dto.CceDto;
import org.jboss.logging.Logger;

public abstract class AbstractByte1Codec<T extends CceDto> implements CceMessageCodec<T> {

    private static final Logger LOG = Logger.getLogger(AbstractByte1Codec.class);
    @Override
    public int getByteCategory() {
        return 1; // Selalu 1 byte
    }

    @Override
    public T decodeBody(byte[] bodyData) {
        if (bodyData == null || bodyData.length < 1) return null;
        short value = (bodyData[0] == (byte) 0xFE || bodyData[0] == (byte) 0xF8)? (short) (bodyData[2] & 0xFF) : (short) (bodyData[1] & 0xFF);

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
        return new byte[] { (byte) value }; // Pembungkusan array langsung di abstract class
    }

    // Class implementasi (misal: GpsStatusCodec) hanya perlu override dua method ini
    protected abstract T mapToDto(short decodedValue);
    protected abstract short mapFromDto(T dto);
}