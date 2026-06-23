package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryResourceListDto implements Jtt808Dto {

    // BYTE (1 byte): Logical Channel Number (0 indicates all channels)
    private int logicalChannelNumber;

    // BCD[6] (6 bytes): YY-MM-DD-HH-MM-SS (All zeros = no start condition)
    private String startTime;

    // BCD[6] (6 bytes): YY-MM-DD-HH-MM-SS (All zeros = no end condition)
    private String endedTime;

    // 64BITS (8 bytes): bit0-31 (Table 75), bit32-63 (Table 74)
    private long alarmFlag;

    // BYTE (1 byte): 0: A/V, 1: Audio, 2: Video, 3: Video or A/V
    private int audioAndVideoResourceType;

    // BYTE (1 byte): 0: All, 1: Main Stream, 2: Sub Stream
    private int streamType;

    // BYTE (1 byte): 0: All, 1: Main Memory, 2: Disaster Recovery
    private int storageDeviceType;
}