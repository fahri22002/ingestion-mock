package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class LocationInformationReportDto implements Jtt808Dto {

    // --- Basic Location Information (28 Bytes) ---
    private long alarmFlag;      // DWORD
    private long status;         // DWORD
    private long latitude;       // DWORD
    private long longitude;      // DWORD
    private int elevation;       // WORD
    private int speed;           // WORD
    private int direction;       // WORD
    private String time;         // BCD[6] (Format: YYMMDDhhmmss)

    // --- Additional Location Information List ---
    private List<AdditionalItem> additionalItems = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalItem {
        private int id;          // BYTE
        private int length;      // BYTE
        private byte[] rawValue; // Raw Byte Data

        // Field spesifik jika ID cocok dengan sub-protokol yang sudah didefinisikan
        private AdasAlarmInfo adasAlarmInfo; // Tersedia jika id == 0x64
        private DsmAlarmInfo dsmAlarmInfo;   // Tersedia jika id == 0x65
    }

    // --- Struktur Data Bersama ---
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmIdentificationNumber {
        private String terminalModelId; // BYTE[7]
        private String time;            // BCD[6] (YYMMDDhhmmss)
        private int index;              // BYTE
        private int attachmentCount;    // BYTE
        private int reserved;           // BYTE
    }

    // --- Struktur ID 0x64 (ADAS) ---
    @Data
    public static class AdasAlarmInfo {
        private long alarmId;               // DWORD
        private int flagStatus;             // BYTE
        private int alarmEventType;         // BYTE
        private int alarmLevel;             // BYTE
        private int precedingVehicleSpeed;  // BYTE
        private int distanceToPreceding;    // BYTE
        private int deviationType;          // BYTE
        private int roadSignType;           // BYTE
        private int roadSignData;           // BYTE
        private int vehicleSpeed;           // BYTE
        private int elevation;              // WORD
        private long latitude;              // DWORD
        private long longitude;             // DWORD
        private String dateTime;            // BCD[6]
        private int vehicleStatus;          // WORD
        private AlarmIdentificationNumber alarmIdentificationNumber;
    }

    // --- Struktur ID 0x65 (DSM) ---
    @Data
    public static class DsmAlarmInfo {
        private long alarmId;               // DWORD
        private int flagStatus;             // BYTE
        private int alarmEventType;         // BYTE
        private int alarmLevel;             // BYTE
        private int drowsinessLevel;        // BYTE
        private byte[] reserved = new byte[4]; // BYTE[4]
        private int vehicleSpeed;           // BYTE
        private int elevation;              // WORD
        private long latitude;              // DWORD
        private long longitude;             // DWORD
        private String dateTime;            // BCD[6]
        private int vehicleStatus;          // WORD
        private AlarmIdentificationNumber alarmIdentificationNumber;
    }
}