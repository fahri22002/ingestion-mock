// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.IgnitionOffEventAuxMsgDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class IgnitionOffEventAuxMsgDto implements CceDto {
    private int parameterId; // Kosong, diisi manual oleh Service saat Encode

    private int version = 1; // Default 0x01

    // Satuan: Meter
    private long tripDistanceMeters;

    // Satuan: Detik
    private long tripDurationSeconds;

    // Satuan: KM/H
    private int averageSpeedKmh;
    private int maxSpeedKmh;

    // Satuan: Persentase murni (%)
    private double tripFuelConsumptionPercent;
}