// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.TpmsDataDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class TpmsDataDto implements CceDto {
    private int parameterId; // Kosong, diisi manual oleh Service saat Encode
    private int group;       // 1 untuk Data 1 (0xFEF2), 2 untuk Data 2 (0xFEF3)

    private List<TpmsItem> items = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TpmsItem {
        private int vehiclePart;       // 0: Head, 1-4: Trailer 1-4
        private int tireNumber;        // 1: First tire, dst...
        private String sensorId;       // Format Hexadecimal (3 Byte)
        private Double tirePressureBar;// Tekanan dalam satuan Bar
        private Integer temperatureC;  // Suhu dalam Celcius

        // Status Flags
        private boolean transmitterBatteryLow;
        private boolean noDataReceived;
        private boolean highAirPressure;
        private boolean lowAirPressure;
        private boolean highTemperature;
        private int alertStatus;       // 0: No alert, 1: Fast leak, 2: Slow leak, 3: Inflation
    }
}