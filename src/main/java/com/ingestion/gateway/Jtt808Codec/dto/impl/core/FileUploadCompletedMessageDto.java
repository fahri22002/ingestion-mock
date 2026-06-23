package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadCompletedMessageDto implements Jtt808Dto {

    // BYTE (1 byte): Panjang nama file
    private int fileNameLength;

    // STRING: Nama file
    private String fileName;

    // BYTE (1 byte): Tipe File (0x00: Picture, 0x01: Audio, 0x02: Video, 0x03: Text, 0x04: Others)
    private int fileType;

    // DWORD (4 byte): Ukuran file yang diunggah
    private long fileSize;
}