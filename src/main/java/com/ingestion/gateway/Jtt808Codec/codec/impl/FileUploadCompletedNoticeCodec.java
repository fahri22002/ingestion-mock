package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.FileUploadCompletedNoticeDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;

@ApplicationScoped
public class FileUploadCompletedNoticeCodec implements Jtt808MessageCodec<FileUploadCompletedNoticeDto> {

    private static final Logger LOG = Logger.getLogger(FileUploadCompletedNoticeCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x1206; // ID Mesej untuk File Upload Completed Notice
    }

    @Override
    public Class<FileUploadCompletedNoticeDto> getSupportedDtoClass() {
        return FileUploadCompletedNoticeDto.class;
    }

    @Override
    public String getCommandName() {
        return "File Upload Completed Notice";
    }

    @Override
    public FileUploadCompletedNoticeDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        FileUploadCompletedNoticeDto dto = new FileUploadCompletedNoticeDto();

        try {
            // 1. WORD (2 byte): Response Serial Number
            dto.setResponseSerialNumber(Short.toUnsignedInt(buf.getShort()));

            // 2. BYTE (1 byte): Result
            dto.setResult(Byte.toUnsignedInt(buf.get()));

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Response Serial Num : %d", dto.getResponseSerialNumber());
            LOG.infof("Result              : %d (0: Succeed, 1: Fail)", dto.getResult());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body File Upload Completed Notice", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(FileUploadCompletedNoticeDto dto) {
        try {
            // Total alokasi buffer konstan: 2 (WORD) + 1 (BYTE) = 3 byte
            ByteBuffer buf = ByteBuffer.allocate(3);

            // 1. WORD: Response Serial Number
            buf.putShort((short) dto.getResponseSerialNumber());

            // 2. BYTE: Result
            buf.put((byte) dto.getResult());

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body File Upload Completed Notice", e);
            return new byte[0];
        }
    }
}