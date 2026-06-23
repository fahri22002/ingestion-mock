// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.BluetoothTempHumiditySensorDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class BluetoothTempHumiditySensorDto implements CceDto {
    private int parameterId;
    private int version = 1; // Default 0x01
    private List<SensorItem> sensors = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorItem {
        private String deviceName;
        private String macAddress;
        private Integer batteryLevel;
        private Double temperature;
        private Double humidity;
        private Double highTempAlarm;
        private Double lowTempAlarm;
        private Double highHumAlarm;
        private Double lowHumAlarm;
    }
}