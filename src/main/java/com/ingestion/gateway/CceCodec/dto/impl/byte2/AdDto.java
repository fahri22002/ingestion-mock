package com.ingestion.gateway.CceCodec.dto.impl.byte2;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class AdDto implements CceDto{
    private int parameterId;
    private int index;
    private int adVolt;
}
