package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeAudioAndVideoTransmissionRequestDto implements Jtt808Dto {

    // BYTE (1 byte): Panjang karakter IP Address
    private int serverIpLength;

    // STRING (n bytes): Alamat IP Server untuk Video/Audio Real-time
    private String serverIp;

    // WORD (2 byte): Port TCP Server
    private int tcpPort;

    // WORD (2 byte): Port UDP Server
    private int udpPort;

    // BYTE (1 byte): Nomor Logical Channel (Refer ke Tabel 69)
    private int logicalChannelNumber;

    // BYTE (1 byte): Tipe Data (0: A/V, 1: Video, 2: Intercom, 3: Monitor, 4: Broadcast, 5: Transparent)
    private int dataType;

    // BYTE (1 byte): Stream Type (0: Main stream, 1: Sub stream)
    private int streamType;
}