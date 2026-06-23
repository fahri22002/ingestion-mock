// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.OverspeedEventInfoDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class OverspeedEventInfoDto implements CceDto {
    private int parameterId; // Kosong, diisi manual oleh Service saat Encode

    // Satuan: Detik sejak 1 Januari 2000, 00:00:00
    private long overspeedStartTime;

    // Titik koordinat awal
    private double overspeedStartLongitude;
    private double overspeedStartLatitude;

    // Satuan: Detik sejak 1 Januari 2000, 00:00:00
    private long overspeedEndTime;

    // Titik koordinat akhir
    private double overspeedEndLongitude;
    private double overspeedEndLatitude;

    // Durasi pelanggaran kecepatan
    private long overspeedDuration; // Satuan: Detik

    // Kecepatan (sudah dikonversi ke KM/H murni)
    private double averageSpeed;
    private double maximumSpeed;
}