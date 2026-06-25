package com.ingestion.gateway.CceCodec.dto.impl.byte1;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class GpsPositioningStatusDto implements CceDto {
    private int parameterId;
    private short gpsPositioningStatus;

    public boolean isValid() {
        return gpsPositioningStatus == 1;
    }

    public String getStatusLabel() {
        return isValid() ? "valid" : "invalid";
    }
}
