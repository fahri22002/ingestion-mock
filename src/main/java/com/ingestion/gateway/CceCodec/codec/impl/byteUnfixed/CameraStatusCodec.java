package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.CameraStatusDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@ApplicationScoped
public class CameraStatusCodec extends AbstractByteUnfixedCodec<CameraStatusDto> {

    private static final Logger LOG = Logger.getLogger(CameraStatusCodec.class);

    @Override
    public Class<CameraStatusDto> getSupportedDtoClass() {
        return CameraStatusDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x49;
    }

    @Override
    public String getParameterName() {
        return "Camera Status";
    }

    @Override
    protected CameraStatusDto mapToDto(byte[] decodedValueBytes) {
        // Validasi panjang: 1 byte (Number) + 8 byte (Status) = 9 byte
        if (decodedValueBytes == null || decodedValueBytes.length < 9) {
            return null;
        }

        CameraStatusDto dto = new CameraStatusDto();
        try {
            dto.setNumber(decodedValueBytes[0] & 0xFF);

            ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes, 1, 8).order(ByteOrder.LITTLE_ENDIAN);
            dto.setStatus(buffer.getLong());

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai CameraStatus", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(CameraStatusDto dto) {
        // Status kamera CCE: total kamera (1 byte) + status flag (8 byte) = 9 bytes
        ByteBuffer buffer = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) dto.getNumber());
        buffer.putLong(dto.getStatus());
        return buffer.array();
    }
}