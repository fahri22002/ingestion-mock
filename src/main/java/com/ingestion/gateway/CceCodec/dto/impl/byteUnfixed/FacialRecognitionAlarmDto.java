// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.FacialRecognitionAlarmDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class FacialRecognitionAlarmDto implements CceDto {
    private int parameterId;
    private int alarmProtocol;
    private int alarmType;
    private String photoName;
}