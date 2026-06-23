package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuerySpecifiedTrackerParametersDto implements Jtt808Dto {

    // BYTE (1 byte): Total number of parameters
    private int totalParameters;

    // BYTE[4*n]: Parameter ID List (Setiap ID bernilai DWORD / 4 byte)
    private List<Long> parameterIds = new ArrayList<>();
}