package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.Data;

@Data
public class TrackerRegistrationDto implements Jtt808Dto {
    private int provinceId;
    private int cityAndCountryId;
    private String manufacturerId;
    private String terminalModel;
    private String terminalModelId;
    private int licensePlateColor;
    private String vehicleIdentification;
}
