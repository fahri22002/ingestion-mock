package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformIssuedRemoteRecordingPlaybackRequestDto implements Jtt808Dto {

    // BYTE (1 byte): Panjang IP
    private int serverIpLength;

    // STRING (n bytes): Server IP Address
    private String serverIp;

    // WORD (2 byte): TCP Port
    private int tcpPort;

    // WORD (2 byte): UDP Port
    private int udpPort;

    // BYTE (1 byte): Logical Channel Number
    private int logicalChannelNumber;

    // BYTE (1 byte): 0: A/V, 1: Audio, 2: Video, 3: Video or A/V
    private int audioAndVideoType;

    // BYTE (1 byte): 0: Main/Sub, 1: Main, 2: Sub
    private int streamType;

    // BYTE (1 byte): 0: Main/Disaster, 1: Main memory, 2: Disaster recovery memory
    private int storageDeviceType;

    // BYTE (1 byte): 0: Normal, 1: Fast Forward, 2: Rewind, 3: Key Frame, 4: Single Frame
    private int playbackMode;

    // BYTE (1 byte): 0: Invalid, 1: 1x, 2: 2x, 3: 4x, 4: 8x, 5: 16x
    private int fastForwardOrRewindMultiplier;

    // BCD[6] (6 bytes): YY-MM-DD-HH-MM-SS
    private String startTime;

    // BCD[6] (6 bytes): YY-MM-DD-HH-MM-SS
    private String endedTime;
}