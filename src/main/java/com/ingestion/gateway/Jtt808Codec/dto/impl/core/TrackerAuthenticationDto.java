package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.Data;

@Data
public class TrackerAuthenticationDto implements Jtt808Dto{
    private int authCodeLength;
    private String authCode;
    private String trackerImei;
    private String softwareVersionNumber;
}
