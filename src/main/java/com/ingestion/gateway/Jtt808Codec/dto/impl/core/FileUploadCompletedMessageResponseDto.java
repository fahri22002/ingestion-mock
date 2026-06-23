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
public class FileUploadCompletedMessageResponseDto implements Jtt808Dto {

    // BYTE (1 byte): Panjang nama file
    private int fileNameLength;

    // STRING: Nama file
    private String fileName;

    // BYTE (1 byte): Tipe File (0x00: Picture, 0x01: Audio, 0x02: Video, 0x03: Text, 0x04: Others)
    private int fileType;

    // BYTE (1 byte): Hasil unggahan (0x00: Done, 0x01: Retransmission Required)
    private int uploadResult;

    // BYTE (1 byte): Jumlah paket retransmisi
    private int retransmissionPacketCount;

    // List paket yang perlu ditransmisikan ulang
    private List<RetransmissionPacket> retransmissionPackets = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetransmissionPacket {
        // DWORD (4 byte): Offset data yang perlu diulang di dalam file
        private long dataOffset;

        // DWORD (4 byte): Panjang data yang perlu diulang
        private long dataLength;
    }
}