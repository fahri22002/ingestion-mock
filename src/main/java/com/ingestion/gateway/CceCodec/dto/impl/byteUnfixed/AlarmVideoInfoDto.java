// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.AlarmVideoInfoDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class AlarmVideoInfoDto implements CceDto {
    private int parameterId;

    private Character group;
    private int version = 1;

    // 3. List untuk menampung video dari berbagai channel
    private List<VideoItem> videoItems = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoItem {
        private int channelNumber; // 1: CH1, 2: CH2, 3: CH3, 4: CH4
        private int channelType;   // 1: ADS, 2: DMS, 3: Ordinary camera
        private String videoName;  // Nama video (maksimal 125 bytes)
    }
}