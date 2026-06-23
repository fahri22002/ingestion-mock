package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.FileUploadCommandDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@ApplicationScoped
public class FileUploadCommandCodec implements Jtt808MessageCodec<FileUploadCommandDto> {

    private static final Logger LOG = Logger.getLogger(FileUploadCommandCodec.class);
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x9206;
    }

    @Override
    public Class<FileUploadCommandDto> getSupportedDtoClass() {
        return FileUploadCommandDto.class;
    }

    @Override
    public String getCommandName() {
        return "File Upload Command";
    }

    @Override
    public FileUploadCommandDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        FileUploadCommandDto dto = new FileUploadCommandDto();

        try {
            // 1. Server Address (k)
            int addrLen = Byte.toUnsignedInt(buf.get());
            dto.setServerAddressLength(addrLen);
            byte[] addrBytes = new byte[addrLen];
            buf.get(addrBytes);
            dto.setServerAddress(new String(addrBytes, GBK));

            // 2. Port
            dto.setPort(Short.toUnsignedInt(buf.getShort()));

            // 3. User Name (l)
            int userLen = Byte.toUnsignedInt(buf.get());
            dto.setUserNameLength(userLen);
            byte[] userBytes = new byte[userLen];
            buf.get(userBytes);
            dto.setUserName(new String(userBytes, GBK));

            // 4. Password (m)
            int passLen = Byte.toUnsignedInt(buf.get());
            dto.setPasswordLength(passLen);
            byte[] passBytes = new byte[passLen];
            buf.get(passBytes);
            dto.setPassword(new String(passBytes, GBK));

            // 5. File Upload Path (n)
            int pathLen = Byte.toUnsignedInt(buf.get());
            dto.setFileUploadPathLength(pathLen);
            byte[] pathBytes = new byte[pathLen];
            buf.get(pathBytes);
            dto.setFileUploadPath(new String(pathBytes, GBK));

            // --- 6. Blok Fixed Bytes (Total 25 byte) ---
            dto.setLogicalChannelNumber(Byte.toUnsignedInt(buf.get()));

            byte[] startTime = new byte[6];
            buf.get(startTime);
            dto.setStartTime(decodeBcd(startTime));

            byte[] endTime = new byte[6];
            buf.get(endTime);
            dto.setEndedTime(decodeBcd(endTime));

            dto.setAlarmFlag(buf.getLong());
            dto.setAudioAndVideoResourceType(Byte.toUnsignedInt(buf.get()));
            dto.setStreamType(Byte.toUnsignedInt(buf.get()));
            dto.setStorageLocation(Byte.toUnsignedInt(buf.get()));
            dto.setTaskExecutionConditions(Byte.toUnsignedInt(buf.get()));

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body File Upload Command", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(FileUploadCommandDto dto) {
        try {
            // Konversi semua string menjadi byte array GBK terlebih dahulu
            byte[] addrBytes = dto.getServerAddress() != null ? dto.getServerAddress().getBytes(GBK) : new byte[0];
            byte[] userBytes = dto.getUserName() != null ? dto.getUserName().getBytes(GBK) : new byte[0];
            byte[] passBytes = dto.getPassword() != null ? dto.getPassword().getBytes(GBK) : new byte[0];
            byte[] pathBytes = dto.getFileUploadPath() != null ? dto.getFileUploadPath().getBytes(GBK) : new byte[0];

            // Hitung ukuran buffer secara absolut:
            // Fixed Fields: 4 (Panjang String) + 2 (Port) + 1 (Ch) + 12 (Time) + 8 (Alarm) + 4 (Lainnya) = 31 bytes
            int totalSize = 31 + addrBytes.length + userBytes.length + passBytes.length + pathBytes.length;

            ByteBuffer buf = ByteBuffer.allocate(totalSize);

            // 1. Server Address
            buf.put((byte) addrBytes.length);
            buf.put(addrBytes);

            // 2. Port
            buf.putShort((short) dto.getPort());

            // 3. User Name
            buf.put((byte) userBytes.length);
            buf.put(userBytes);

            // 4. Password
            buf.put((byte) passBytes.length);
            buf.put(passBytes);

            // 5. File Upload Path
            buf.put((byte) pathBytes.length);
            buf.put(pathBytes);

            // 6. Fixed Fields
            buf.put((byte) dto.getLogicalChannelNumber());
            buf.put(encodeBcd(dto.getStartTime()));
            buf.put(encodeBcd(dto.getEndedTime()));
            buf.putLong(dto.getAlarmFlag());
            buf.put((byte) dto.getAudioAndVideoResourceType());
            buf.put((byte) dto.getStreamType());
            buf.put((byte) dto.getStorageLocation());
            buf.put((byte) dto.getTaskExecutionConditions());

            return buf.array();
        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body File Upload Command", e);
            throw e;
        }
    }

    private String decodeBcd(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02X", x));
        return sb.toString();
    }

    private byte[] encodeBcd(String s) {
        if (s == null) s = "000000000000";
        byte[] b = new byte[6];
        String p = String.format("%-12s", s).replace(' ', '0');
        for (int i = 0; i < 6; i++) {
            b[i] = (byte) Integer.parseInt(p.substring(2 * i, 2 * i + 2), 16);
        }
        return b;
    }
}