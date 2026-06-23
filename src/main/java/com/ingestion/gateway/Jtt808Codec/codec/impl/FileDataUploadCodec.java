package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.dto.impl.core.FileDataUploadDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@ApplicationScoped
public class FileDataUploadCodec {

    private static final Logger LOG = Logger.getLogger(FileDataUploadCodec.class);
    private static final Charset GBK = Charset.forName("GBK");

    public static final long FRAME_HEADER_MAGIC = 0x30316364L;

    // Hapus getSupportedMessageId(), getSupportedDtoClass(), dan getCommandName()

    public FileDataUploadDto decodeStream(byte[] rawStreamData) {
        ByteBuffer buf = ByteBuffer.wrap(rawStreamData);
        FileDataUploadDto dto = new FileDataUploadDto();

        try {
            long frameHeader = Integer.toUnsignedLong(buf.getInt());
            dto.setFrameHeaderIdentifier(frameHeader);

            if (frameHeader != FRAME_HEADER_MAGIC) {
                LOG.warnf("Frame Header tidak valid! Expected: 0x%X, Actual: 0x%X", FRAME_HEADER_MAGIC, frameHeader);
            }

            byte[] fileNameBytes = new byte[50];
            buf.get(fileNameBytes);
            dto.setFileName(new String(fileNameBytes, GBK).trim());

            long dataOffset = Integer.toUnsignedLong(buf.getInt());
            dto.setDataOffset(dataOffset);

            long dataLength = Integer.toUnsignedLong(buf.getInt());
            dto.setDataLength(dataLength);

            int actualLengthToRead = Math.min((int) dataLength, buf.remaining());
            byte[] dataBody = new byte[actualLengthToRead];
            buf.get(dataBody);
            dto.setDataBody(dataBody);

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak stream File Data Upload", e);
            return null;
        }
        return dto;
    }

    public byte[] encodeStream(FileDataUploadDto dto) {
        try {
            byte[] dataBody = dto.getDataBody() != null ? dto.getDataBody() : new byte[0];
            long dataLength = dataBody.length;

            ByteBuffer buf = ByteBuffer.allocate(4 + 50 + 4 + 4 + dataBody.length);

            buf.putInt((int) FRAME_HEADER_MAGIC);

            byte[] fileNamePadded = new byte[50];
            if (dto.getFileName() != null) {
                byte[] rawName = dto.getFileName().getBytes(GBK);
                System.arraycopy(rawName, 0, fileNamePadded, 0, Math.min(rawName.length, 50));
            }
            buf.put(fileNamePadded);

            buf.putInt((int) dto.getDataOffset());
            buf.putInt((int) dataLength);
            buf.put(dataBody);

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode stream File Data Upload", e);
            return new byte[0];
        }
    }
}