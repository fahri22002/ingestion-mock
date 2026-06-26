package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeAudioAndVideoStreamDto implements Jtt808Dto {

    // DWORD (4 byte): Fixed value 0x30316364
    private long frameHeaderIdentifier;

    // BYTE (1 byte): V (2 bits), P (1 bit), X (1 bit), CC (4 bits)
    private int v = 2;
    private int p = 0;
    private int x = 0;
    private int cc = 1;

    // BYTE (1 byte): M (1 bit), PT (7 bits)
    private int m;
    private int pt; // Rujuk kepada Payload Type dari Jadual 72

    // WORD (2 byte): Sequence Number
    private int packetSequenceNumber;

    // BCD[6] (6 byte): SIM card number (6 byte diselaraskan mengikut offset jadual 8 -> 14)
    private String simCardNumber;

    // BYTE (1 byte): Logical Channel Number
    private int logicalChannelNumber;

    // BYTE (1 byte): Data Type (4 bits) & Fragmentation processing flag (4 bits)
    // Data Type: 0 (I-frame), 1 (P-frame), 2 (B-frame), 3 (Audio), 4 (Transparent)
    private int dataType;
    // Fragmentation: 0 (Atomic), 1 (First), 2 (Last), 3 (Intermediate)
    private int fragmentationFlag;

    // BYTE[8] (8 byte): Timestamp (Diabaikan jika dataType == 4)
    private long timestamp;

    // WORD (2 byte): Last I Frame Interval (Diabaikan jika dataType bukan 0,1,2)
    private int lastIFrameInterval;

    // WORD (2 byte): Last Frame Interval (Diabaikan jika dataType bukan 0,1,2)
    private int lastFrameInterval;

    // WORD (2 byte): Length of Data Body
    private int dataBodyLength;

    // BYTE[n]: Data Body
    private byte[] dataBody;

    // Tambahkan di dalam RealTimeAudioAndVideoStreamDto
    private int totalFrameLength;

}