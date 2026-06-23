// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.TemperatureSensorInfoDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class TemperatureSensorInfoDto implements CceDto {
    private int parameterId; // Kosong, diisi manual oleh Service saat Encode

    private int version = 1; // Default version 1

    private List<TemperatureSensor> sensors = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemperatureSensor {
        private int sensorNumber; // 0 indicates unregistered
        private String serialNumber; // Format Hex String (8 byte)
        private double temperatureCelcius; // Suhu asli (dibagi 100 dari nilai mentah)
    }
}