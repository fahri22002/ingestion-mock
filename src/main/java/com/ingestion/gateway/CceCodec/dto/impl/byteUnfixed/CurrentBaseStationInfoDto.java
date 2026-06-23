package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class CurrentBaseStationInfoDto implements CceDto {
    private int parameterId;
    private int mcc;
    private int mnc;
    private int lac;
    private long cellId;
    private short rxLevel;
}