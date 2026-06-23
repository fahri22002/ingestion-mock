package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class AlarmAttachmentsInformationMessageDto implements Jtt808Dto {

    // BYTE[7]: Terminal Model ID
    private String terminalModelId;

    // BYTE[16]: Alarm Identification Number
    private AlarmIdentificationNumber alarmIdentificationNumber;

    // BYTE[32]: Unique number assigned by the platform to the alarm
    private String alarmNo;

    // BYTE (1 byte): 0x00: Normal, 0x01: Re-uploaded
    private int informationType;

    // BYTE (1 byte): Number of Attachments
    private int attachmentCount;

    // List of Attachment Information
    private List<AttachmentInformation> attachmentInformationList = new ArrayList<>();

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttachmentInformation {
        private int fileNameLength; // BYTE
        private String fileName;    // STRING
        private long fileSize;      // DWORD
    }
}