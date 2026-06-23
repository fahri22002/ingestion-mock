// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.AspcPeopleCounterDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class AspcPeopleCounterDto implements CceDto {
    // Kosong, wajib diisi manual oleh Service (contoh: dto.setParameterId(0xFE96))
    private int parameterId;

    private int version = 1;

    // List dinamis untuk sensor 1 hingga 4
    private List<SensorData> sensors = new ArrayList<>();

    // Blok rekapitulasi data seluruh sensor
    private AllSensorData allSensorData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SensorData {
        private int number;       // Sensor label
        private int doorNumber;   // 0:NULL, 1:door1, 2:door2, 3:door3, 4:door4
        private int state;        // 0:invalid, 1:IO open, 2:IO shutdown, 3:IN Geo, 4:OUT Geo

        // Menggunakan tipe "long" untuk menampung dword (4 byte unsigned)
        private long upCar;       // Number of people getting on this time
        private long downCar;     // Number of people getting off this time
        private long allUpCar;    // Total number of people getting on
        private long allDownCar;  // Total number of people getting off
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllSensorData {
        private long upCar;
        private long downCar;
        private long allUpCar;
        private long allDownCar;
        private long surplus;     // Number of people left in the car
    }
}