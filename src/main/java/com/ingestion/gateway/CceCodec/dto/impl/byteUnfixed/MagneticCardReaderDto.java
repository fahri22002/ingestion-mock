// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.MagneticCardReaderDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class MagneticCardReaderDto implements CceDto {
    private int parameterId; // Kosong, diisi oleh Service
    private String rfidData; // Menyimpan data kartu magnetik
}