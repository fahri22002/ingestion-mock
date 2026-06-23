package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackerUploadOfAudioAndVideoResourceListDto implements Jtt808Dto {

    // WORD (2 byte): Serial number corresponding to the query command
    private int responseSerialNumber;

    // DWORD (4 byte): Total number of resources
    private long totalResources;

    // List of resources (Setiap item bersaiz 28 byte)
    private List<ResourceItem> resourceList = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceItem {
        // BYTE (1 byte): Logical Channel Number
        private int logicalChannelNumber;

        // BCD[6] (6 byte): YY-MM-DD-HH-MM-SS
        private String startTime;

        // BCD[6] (6 byte): YY-MM-DD-HH-MM-SS
        private String endedTime;

        // 64BITS (8 byte): Alarm Flag
        private long alarmFlag;

        // BYTE (1 byte): 0: A/V, 1: Audio, 2: Video
        private int audioAndVideoResourceType;

        // BYTE (1 byte): 1: Main stream, 2: Sub stream
        private int streamType;

        // BYTE (1 byte): 1: Main memory, 2: Disaster recovery
        private int storageDeviceType;

        // DWORD (4 byte): Unit: Byte
        private long fileSize;
    }
}