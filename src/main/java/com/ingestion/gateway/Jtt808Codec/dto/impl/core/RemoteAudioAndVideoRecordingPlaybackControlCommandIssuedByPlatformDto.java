package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto implements Jtt808Dto {

    // BYTE (1 byte): Audio and Video Channel Number
    private int audioAndVideoChannelNumber;

    // BYTE (1 byte): 0: Start, 1: Pause, 2: Stop, 3: Fast Forward, 4: Rewind, 5: Drag, 6: Key Frame
    private int playbackControl;

    // BYTE (1 byte): 0: Invalid, 1: 1x, 2: 2x, 3: 4x, 4: 8x, 5: 16x
    private int fastForwardOrRewindMultiplier;

    // BCD[6] (6 bytes): YY-MM-DD-HH-MM-SS (Valid only when playbackControl is 5)
    private String dragPlaybackPosition;
}