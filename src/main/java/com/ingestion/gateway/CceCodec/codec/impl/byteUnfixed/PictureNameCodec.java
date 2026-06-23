// File: com.ingestion.gateway.CceCoder.codec.impl.byte8.PictureNameCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte8Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.PictureNameDto;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class PictureNameCodec extends AbstractByte8Codec<PictureNameDto> {

    // Waktu dasar Meitrack (1 Januari 2000, 00:00:00)
    private static final LocalDateTime EPOCH_2000 = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
    // Format nama file: YYMMDDHHmmss (Contoh: 130513024323)
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    @Override
    public Class<PictureNameDto> getSupportedDtoClass() {
        return PictureNameDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x28;
    }

    @Override
    public String getParameterName() {
        return "Picture name";
    }

    @Override
    protected PictureNameDto mapToDto(byte[] decodedValueBytes) {
        if (decodedValueBytes == null || decodedValueBytes.length < 8) {
            return null;
        }

        PictureNameDto dto = new PictureNameDto();

        // 1. Baca DWORD Pertama (Timestamp offset) - Little Endian (Index 0-3)
        long timestampOffset = (decodedValueBytes[0] & 0xFFL) |
                ((decodedValueBytes[1] & 0xFFL) << 8) |
                ((decodedValueBytes[2] & 0xFFL) << 16) |
                ((decodedValueBytes[3] & 0xFFL) << 24);
        dto.setTimestampOffset(timestampOffset);

        // 2. Baca DWORD Kedua (File Suffix) - Little Endian (Index 4-7)
        long fileSuffixValue = (decodedValueBytes[4] & 0xFFL) |
                ((decodedValueBytes[5] & 0xFFL) << 8) |
                ((decodedValueBytes[6] & 0xFFL) << 16) |
                ((decodedValueBytes[7] & 0xFFL) << 24);
        dto.setFileSuffixValue(fileSuffixValue);

        // 3. Generate Nama File
        // Tambahkan detik ke tanggal dasar (Jan 1, 2000)
        LocalDateTime fileDate = EPOCH_2000.plusSeconds(timestampOffset);
        String dateString = fileDate.format(FILE_DATE_FORMATTER);

        // Ubah nilai suffix menjadi string Hexadecimal (Uppercase)
        String suffixString = String.format("%X", fileSuffixValue);

        // Gabungkan
        dto.setGeneratedFileName(dateString + "_" + suffixString + ".jpg");

        return dto;
    }

    @Override
    protected byte[] mapFromDto(PictureNameDto dto) {
        byte[] bytes = new byte[8];

        long time = dto.getTimestampOffset();
        long suffix = dto.getFileSuffixValue();

        // 1. Tulis DWORD Pertama (Timestamp) - Little Endian
        bytes[0] = (byte) (time & 0xFF);
        bytes[1] = (byte) ((time >> 8) & 0xFF);
        bytes[2] = (byte) ((time >> 16) & 0xFF);
        bytes[3] = (byte) ((time >> 24) & 0xFF);

        // 2. Tulis DWORD Kedua (Suffix) - Little Endian
        bytes[4] = (byte) (suffix & 0xFF);
        bytes[5] = (byte) ((suffix >> 8) & 0xFF);
        bytes[6] = (byte) ((suffix >> 16) & 0xFF);
        bytes[7] = (byte) ((suffix >> 24) & 0xFF);

        return bytes;
    }
}