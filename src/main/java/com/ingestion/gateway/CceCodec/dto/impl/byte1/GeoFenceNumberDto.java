package com.ingestion.gateway.CceCodec.dto.impl.byte1;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class GeoFenceNumberDto implements CceDto{
    private int parameterId;
	private short geoFenceNumber;
}
