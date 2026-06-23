// File: com.ingestion.gateway.CceCoder.dto.impl.byte2.StorageStatusDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class StorageStatusDto implements CceDto {
    private int parameterId; // Kosong, diisi oleh Service
    private int version;
    // 0: Device, 1: M2, 2: SD1, 3: SD2, 4: Backup, 5: Hard Disk
    private int tfType;

    // 1: Full, 2: No device, 3: R/W Error, 4: Partition Abnormal, 5: Removed, 6: Bad Blocks, 7: Insufficient Space
    private int alarmType;
}