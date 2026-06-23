package com.ingestion.gateway.CceCodec.dto.impl.byte4;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class VehicleBatteryVoltDto implements CceDto{
    private int parameterId;
    private long vehicleBatteryVolt;
}
