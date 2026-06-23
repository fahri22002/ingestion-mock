package com.ingestion.gateway.CceCodec.dto.impl.byte1;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class TachographPerformanceDto implements CceDto{
    private int parameterId;
	private short tachographPerformance;
}
