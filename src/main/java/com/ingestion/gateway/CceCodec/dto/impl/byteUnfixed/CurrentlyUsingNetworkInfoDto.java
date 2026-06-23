// File: com.ingestion.gateway.CceCoder.dto.impl.byteUnfixed.CurrentlyUsingNetworkInfoDto.java
package com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class CurrentlyUsingNetworkInfoDto implements CceDto {
    private int parameterId;
    private int version = 1;
    private int networkType;
    private String descriptor;
}