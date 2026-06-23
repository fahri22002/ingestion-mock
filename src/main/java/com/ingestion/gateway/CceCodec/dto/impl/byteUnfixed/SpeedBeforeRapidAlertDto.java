// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.SpeedBeforeRapidAlertDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SpeedBeforeRapidAlertDto implements CceDto {
    private int parameterId;
    private int unitIntervalMs; // Interval antar perekaman kecepatan dalam milidetik (ms)
    private List<Integer> speedValues = new ArrayList<>(); // Daftar kecepatan dalam KM/H
}