package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadControlDto implements Jtt808Dto {

    // WORD (2 byte): Serial number corresponding to the platform file upload message
    private int responseSerialNumber;

    // BYTE (1 byte): 0: Pause; 1: Continue; 2: Cancel
    private int uploadControl;
}