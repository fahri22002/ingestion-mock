package com.ingestion.gateway.CceCodec.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class CceMessageEnvelope {
    private CceIdentifier identifier;
    private String imei;
    private int cacheRemaining;
    private int numPackets;

    // Ini akan menampung CameraStatusDto, SpeedDto, dll.
    private List<CceDto> parameters = new ArrayList<>();
}