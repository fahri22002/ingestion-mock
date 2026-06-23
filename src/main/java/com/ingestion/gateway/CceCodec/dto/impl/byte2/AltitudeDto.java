package com.ingestion.gateway.CceCodec.dto.impl.byte2;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class AltitudeDto implements CceDto{
    private int parameterId;
    private int altitude;
}
