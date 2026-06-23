package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class AlarmAttachmentsUploadCommandDto implements Jtt808Dto {

    // BYTE (1 byte): Length k
    private int serverIpLength;

    // STRING (k bytes): Server IP Address
    private String serverIp;

    // WORD (2 byte): Server port number used for TCP transmission
    private int tcpPort;

    // WORD (2 byte): Server port number used for UDP transmission
    private int udpPort;

    // BYTE[16] (16 byte): Alarm Identification Number (sesuai Tabel 22)
    private AlarmIdentificationNumber alarmIdentificationNumber;

    // BYTE[32] (32 byte): Unique number assigned by the platform
    private String alarmNo;

    // BYTE[16] (16 byte): Reserved
    private byte[] reserved = new byte[16];

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlarmIdentificationNumber {
        private String terminalModelId; // BYTE[7]
        private String time;            // BCD[6] (YYMMDDhhmmss)
        private int index;              // BYTE
        private int attachmentCount;    // BYTE
        private int reserved;           // BYTE
    }
}