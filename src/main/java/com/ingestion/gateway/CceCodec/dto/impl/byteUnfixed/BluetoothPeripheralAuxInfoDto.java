// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.BluetoothPeripheralAuxInfoDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class BluetoothPeripheralAuxInfoDto implements CceDto {
    private int parameterId;

    private int version = 1;
    private int alarmType;

    // Untuk Tipe 01 - 10
    private String deviceName;
    private String macAddress;

    // Untuk Tipe 01 - 05, dan 08
    private Integer batteryLevel;

    // Untuk Tipe 01 - 05
    private Double temperature;
    private Double humidity;

    // Untuk Tipe 08
    private Byte signalStrength;

    // Untuk Tipe 11 dan 12
    private Integer major;
    private Integer minor;
}