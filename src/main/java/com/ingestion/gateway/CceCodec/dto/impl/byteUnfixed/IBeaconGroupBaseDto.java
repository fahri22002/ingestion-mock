package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class IBeaconGroupBaseDto implements CceDto {
    private int parameterId;

    private Character group;

    private int version = 1; // Default 0x01
    private List<IBeaconItem> beacons = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IBeaconItem {
        private String deviceName;
        private String macAddress;
        private int batteryLevel;
        private byte signalStrength; // Menggunakan byte (signed) untuk nilai minus RSSI
    }
}