// File: com.ingestion.gateway.CceCoder.codec.impl.byte3.TemperatureSensorCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte3Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.TemperatureSensorDto;

public abstract class TemperatureSensorBaseCodec extends AbstractByte3Codec<TemperatureSensorDto> {

    @Override
    public Class<TemperatureSensorDto> getSupportedDtoClass() {
        return TemperatureSensorDto.class;
    }

    @Override
    protected TemperatureSensorDto mapToDto(byte[] decodedValueBytes) {
        // Pastikan kita mendapatkan 3 byte murni
        if (decodedValueBytes == null || decodedValueBytes.length < 3) {
            return null;
        }

        TemperatureSensorDto dto = new TemperatureSensorDto();

        // 1. Baca byte pertama sebagai Sensor Number (Unsigned 1-Byte)
        dto.setSensorNumber(decodedValueBytes[0] & 0xFF);

        // 2. Baca Suhu (Signed 2-Bytes, Little-Endian)
        // Penggunaan (short) sangat penting agar nilai hex seperti 0xFFxx terbaca sebagai minus
        short rawTemperature = (short) ((decodedValueBytes[1] & 0xFF) | (decodedValueBytes[2] << 8));

        // Konversi ke desimal sesuai format Meitrack
        dto.setTemperature(rawTemperature / 100.0);

        return dto;
    }

    @Override
    protected byte[] mapFromDto(TemperatureSensorDto dto) {
        byte[] bytes = new byte[3];

        // 1. Tulis byte Sensor Number
        bytes[0] = (byte) (dto.getSensorNumber() & 0xFF);

        // 2. Tulis byte Suhu
        // Kembalikan ke nilai mentah (kali 100)
        short rawTemperature = (short) Math.round(dto.getTemperature() * 100);

        // Pecah jadi 2 byte Little-Endian
        bytes[1] = (byte) (rawTemperature & 0xFF);         // Low byte
        bytes[2] = (byte) ((rawTemperature >> 8) & 0xFF);  // High byte

        return bytes;
    }
}