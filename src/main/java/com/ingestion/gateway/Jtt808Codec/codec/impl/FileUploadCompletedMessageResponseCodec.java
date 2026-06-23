package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.FileUploadCompletedMessageResponseDto;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.FileUploadCompletedMessageResponseDto.RetransmissionPacket;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FileUploadCompletedMessageResponseCodec implements Jtt808MessageCodec<FileUploadCompletedMessageResponseDto> {

    private static final Logger LOG = Logger.getLogger(FileUploadCompletedMessageResponseCodec.class);

    // Protokol menetapkan tipe STRING menggunakan encoding GBK
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x9212; // ID untuk File Upload Completed Message Response
    }

    @Override
    public Class<FileUploadCompletedMessageResponseDto> getSupportedDtoClass() {
        return FileUploadCompletedMessageResponseDto.class;
    }

    @Override
    public String getCommandName() {
        return "File Upload Completed Message Response";
    }

    @Override
    public FileUploadCompletedMessageResponseDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        FileUploadCompletedMessageResponseDto dto = new FileUploadCompletedMessageResponseDto();

        try {
            // 1. BYTE (1 byte): File Name Length
            int fileNameLen = Byte.toUnsignedInt(buf.get());
            dto.setFileNameLength(fileNameLen);

            // 2. STRING (n bytes): File Name
            byte[] fileNameBytes = new byte[fileNameLen];
            buf.get(fileNameBytes);
            dto.setFileName(new String(fileNameBytes, GBK).trim());

            // 3. BYTE (1 byte): File Type
            int fileType = Byte.toUnsignedInt(buf.get());
            dto.setFileType(fileType);

            // 4. BYTE (1 byte): Upload Result
            int uploadResult = Byte.toUnsignedInt(buf.get());
            dto.setUploadResult(uploadResult);

            // 5. BYTE (1 byte): Retransmission Packet Count
            int packetCount = Byte.toUnsignedInt(buf.get());
            dto.setRetransmissionPacketCount(packetCount);

            // 6. List of Data Packets for Retransmission (8 byte per packet)
            List<RetransmissionPacket> packets = new ArrayList<>();
            for (int i = 0; i < packetCount; i++) {
                if (buf.remaining() >= 8) { // Memastikan ada cukup sisa byte (4 byte Offset + 4 byte Length)
                    RetransmissionPacket packet = new RetransmissionPacket();

                    // DWORD (4 byte): Data Offset
                    packet.setDataOffset(Integer.toUnsignedLong(buf.getInt()));

                    // DWORD (4 byte): Data Length
                    packet.setDataLength(Integer.toUnsignedLong(buf.getInt()));

                    packets.add(packet);
                }
            }
            dto.setRetransmissionPackets(packets);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("File Name      : %s (Len: %d)", dto.getFileName(), dto.getFileNameLength());
            LOG.infof("File Type      : %d", dto.getFileType());
            LOG.infof("Upload Result  : %d", dto.getUploadResult());
            LOG.infof("Retransmission : %d packet(s)", packets.size());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body File Upload Completed Message Response", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(FileUploadCompletedMessageResponseDto dto) {
        try {
            byte[] fileNameBytes = dto.getFileName() != null ? dto.getFileName().getBytes(GBK) : new byte[0];
            int fileNameLen = fileNameBytes.length;

            List<RetransmissionPacket> packets = dto.getRetransmissionPackets() != null
                    ? dto.getRetransmissionPackets() : new ArrayList<>();
            int packetCount = packets.size();

            // Total alokasi buffer:
            // 1 (Name Length) + fileNameLen + 1 (File Type) + 1 (Result) + 1 (Packet Count) + (8 * packetCount)
            int totalSize = 1 + fileNameLen + 1 + 1 + 1 + (8 * packetCount);
            ByteBuffer buf = ByteBuffer.allocate(totalSize);

            // 1. BYTE: File Name Length
            buf.put((byte) fileNameLen);

            // 2. STRING: File Name
            buf.put(fileNameBytes);

            // 3. BYTE: File Type
            buf.put((byte) dto.getFileType());

            // 4. BYTE: Upload Result
            buf.put((byte) dto.getUploadResult());

            // 5. BYTE: Retransmission Packet Count
            buf.put((byte) packetCount);

            // 6. List of Data Packets for Retransmission
            for (RetransmissionPacket packet : packets) {
                // DWORD: Data Offset
                buf.putInt((int) packet.getDataOffset());

                // DWORD: Data Length
                buf.putInt((int) packet.getDataLength());
            }

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body File Upload Completed Message Response", e);
            return new byte[0];
        }
    }
}