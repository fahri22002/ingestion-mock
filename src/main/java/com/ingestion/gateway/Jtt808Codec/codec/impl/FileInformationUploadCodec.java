package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.FileInformationUploadDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@ApplicationScoped
public class FileInformationUploadCodec implements Jtt808MessageCodec<FileInformationUploadDto> {

    private static final Logger LOG = Logger.getLogger(FileInformationUploadCodec.class);

    // Protokol menetapkan tipe STRING menggunakan encoding GBK
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x1211; // ID untuk File Information Upload
    }

    @Override
    public Class<FileInformationUploadDto> getSupportedDtoClass() {
        return FileInformationUploadDto.class;
    }

    @Override
    public String getCommandName() {
        return "File Information Upload";
    }

    @Override
    public FileInformationUploadDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        FileInformationUploadDto dto = new FileInformationUploadDto();

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

            // 4. DWORD (4 byte): File Size
            long fileSize = Integer.toUnsignedLong(buf.getInt());
            dto.setFileSize(fileSize);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("File Name : %s (Len: %d)", dto.getFileName(), dto.getFileNameLength());
            LOG.infof("File Type : %d", dto.getFileType());
            LOG.infof("File Size : %d bytes", dto.getFileSize());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body File Information Upload", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(FileInformationUploadDto dto) {
        try {
            byte[] fileNameBytes = dto.getFileName() != null ? dto.getFileName().getBytes(GBK) : new byte[0];
            int fileNameLen = fileNameBytes.length;

            // Total alokasi buffer: 1 (Length) + fileNameLen + 1 (Type) + 4 (Size)
            ByteBuffer buf = ByteBuffer.allocate(1 + fileNameLen + 1 + 4);

            // 1. BYTE: File Name Length
            buf.put((byte) fileNameLen);

            // 2. STRING: File Name
            buf.put(fileNameBytes);

            // 3. BYTE: File Type
            buf.put((byte) dto.getFileType());

            // 4. DWORD: File Size
            buf.putInt((int) dto.getFileSize());

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body File Information Upload", e);
            return new byte[0];
        }
    }
}