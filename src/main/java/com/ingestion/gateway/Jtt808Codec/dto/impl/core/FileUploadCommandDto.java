package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadCommandDto implements Jtt808Dto {

    // BYTE (1 byte): Panjang Server Address (k)
    private int serverAddressLength;

    // STRING (k byte): FTP Server Address
    private String serverAddress;

    // WORD (2 byte): FTP Server Port
    private int port;

    // BYTE (1 byte): Panjang User Name (l)
    private int userNameLength;

    // STRING (l byte): FTP User Name
    private String userName;

    // BYTE (1 byte): Panjang Password (m)
    private int passwordLength;

    // STRING (m byte): FTP Password
    private String password;

    // BYTE (1 byte): Panjang File Upload Path (n)
    private int fileUploadPathLength;

    // STRING (n byte): File Upload Path
    private String fileUploadPath;

    // BYTE (1 byte): Logical Channel Number
    private int logicalChannelNumber;

    // BCD[6] (6 byte): YY-MM-DD-HH-MM-SS
    private String startTime;

    // BCD[6] (6 byte): YY-MM-DD-HH-MM-SS
    private String endedTime;

    // 64BITS (8 byte): Alarm Flag
    private long alarmFlag;

    // BYTE (1 byte): 0: A/V, 1: Audio, 2: Video, 3: Video or A/V
    private int audioAndVideoResourceType;

    // BYTE (1 byte): 0: Main/Sub, 1: Main Stream, 2: Sub Stream
    private int streamType;

    // BYTE (1 byte): 0: Main/Disaster, 1: Main memory, 2: Disaster recovery memory
    private int storageLocation;

    // BYTE (1 byte): Task Execution Conditions (bit0: WIFI, bit1: LAN, bit2: 3G/4G)
    private int taskExecutionConditions;
}