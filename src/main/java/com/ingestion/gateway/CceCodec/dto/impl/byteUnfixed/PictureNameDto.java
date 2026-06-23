// File: com.ingestion.gateway.CceCoder.dto.impl.byte8.PictureNameDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class PictureNameDto implements CceDto {
    private int parameterId;
    private long timestampOffset;
    private long fileSuffixValue;
    private String generatedFileName;
}