package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDataUploadDto implements Jtt808Dto {

    // DWORD (4 byte): Fixed value 0x30316364
    private long frameHeaderIdentifier;

    // BYTE[50] (50 byte): File Name
    private String fileName;

    // DWORD (4 byte): Current data offset of the transmitted file
    private long dataOffset;

    // DWORD (4 byte): Length of the payload data
    private long dataLength;

    // BYTE[n]: Data Body (Default 64K or actual size)
    private byte[] dataBody;
}