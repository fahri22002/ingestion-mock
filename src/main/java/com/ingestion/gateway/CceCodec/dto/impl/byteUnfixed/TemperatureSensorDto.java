
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class TemperatureSensorDto implements CceDto {
    private int parameterId;
    private int sensorNumber;
    private double temperature;
}