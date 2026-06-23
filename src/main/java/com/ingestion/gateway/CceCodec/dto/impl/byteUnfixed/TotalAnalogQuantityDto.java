// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.TotalAnalogQuantityDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class TotalAnalogQuantityDto implements CceDto {
    private int parameterId;
    private List<AnalogItem> analogItems = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalogItem {
        private int adNumber;
        private int voltageMv;
    }
}