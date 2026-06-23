package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.Data;

@Data
public class TrackerRegistrationResponseDto implements Jtt808Dto {
    private int responseSerialNumber;
    private int result;
    private String authCode;
}
