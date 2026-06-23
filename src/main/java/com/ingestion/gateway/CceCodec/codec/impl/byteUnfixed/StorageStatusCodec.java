// File: com.ingestion.gateway.CceCoder.codec.impl.byte3.StorageStatusCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte3Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.StorageStatusDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StorageStatusCodec extends AbstractByte3Codec<StorageStatusDto> {

    @Override
    public Class<StorageStatusDto> getSupportedDtoClass() {
        return StorageStatusDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x72;
    }

    @Override
    public String getParameterName() {
        return "Storage Status";
    }

    @Override
    protected StorageStatusDto mapToDto(byte[] decodedValueBytes) {
        StorageStatusDto dto = new StorageStatusDto();

        // decodedValueBytes sudah dipastikan persis 3 byte oleh kelas Abstract.
        // Index 0: Version
        // Index 1: TF_type
        // Index 2: Alarm_type

        dto.setVersion(decodedValueBytes[0] & 0xFF);
        dto.setTfType(decodedValueBytes[1] & 0xFF);
        dto.setAlarmType(decodedValueBytes[2] & 0xFF);

        return dto;
    }

    @Override
    protected byte[] mapFromDto(StorageStatusDto dto) {
        // Cukup rakit ke dalam array berukuran 3 byte sesuai urutan Little-Endian
        return new byte[] {
                (byte) (dto.getVersion() & 0xFF),
                (byte) (dto.getTfType() & 0xFF),
                (byte) (dto.getAlarmType() & 0xFF)
        };
    }
}