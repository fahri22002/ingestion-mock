package com.ingestion.gateway.CceCodec.dto.impl.byte2;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class HdopDto implements CceDto{
    private int parameterId;
    private int hdop;
}
