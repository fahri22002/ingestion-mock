package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class CameraStatusDto implements CceDto {
    private int parameterId;

    private int number;
    private long status;

    public boolean isCameraConnected(int channel) {
        if (channel < 1 || channel > 64) {
            throw new IllegalArgumentException("Channel kamera harus antara 1 - 64");
        }
        // Geser angka 1 ke kiri sebanyak (channel - 1), lalu cek dengan status
        long mask = 1L << (channel - 1);
        return (status & mask) != 0;
    }
}