package com.ingestion.gateway.CceCodec.dto.impl.byte1;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class EventCodeDto implements CceDto{
    private int parameterId;
    private short eventCode;
}
