package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeAudioAndVideoTransmissionControlDto implements Jtt808Dto {

    // BYTE (1 byte): Logical Channel Number
    private int logicalChannelNumber;

    // BYTE (1 byte): Control Command (0: Turn Off, 1: Switch, 2: Pause, 3: Resume, 4: Turn Off Intercom)
    private int controlCommand;

    // BYTE (1 byte): Turn Off Audio and Video Type (0: Turn Off A/V, 1: Audio only, 2: Video only)
    private int turnOffAudioAndVideoType;

    // BYTE (1 byte): Switch Stream Type (0: Main stream, 1: Sub stream)
    private int switchStreamType;
}