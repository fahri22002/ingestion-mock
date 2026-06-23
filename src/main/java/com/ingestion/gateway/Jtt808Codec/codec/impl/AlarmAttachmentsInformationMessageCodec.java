package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.AlarmAttachmentsInformationMessageDto;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.AlarmAttachmentsInformationMessageDto.AlarmIdentificationNumber;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.AlarmAttachmentsInformationMessageDto.AttachmentInformation;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AlarmAttachmentsInformationMessageCodec implements Jtt808MessageCodec<AlarmAttachmentsInformationMessageDto> {

    private static final Logger LOG = Logger.getLogger(AlarmAttachmentsInformationMessageCodec.class);
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x1210; // ID untuk Alarm Attachments Information Message
    }

    @Override
    public Class<AlarmAttachmentsInformationMessageDto> getSupportedDtoClass() {
        return AlarmAttachmentsInformationMessageDto.class;
    }

    @Override
    public String getCommandName() {
        return "Alarm Attachments Information Message";
    }

    @Override
    public AlarmAttachmentsInformationMessageDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        AlarmAttachmentsInformationMessageDto dto = new AlarmAttachmentsInformationMessageDto();

        try {
            // 1. BYTE[7]: Terminal Model ID
            byte[] terminalIdBytes = new byte[7];
            buf.get(terminalIdBytes);
            dto.setTerminalModelId(new String(terminalIdBytes, GBK).trim());

            // 2. BYTE[16]: Alarm Identification Number
            dto.setAlarmIdentificationNumber(parseAlarmIdNumber(buf));

            // 3. BYTE[32]: Alarm No.
            byte[] alarmNoBytes = new byte[32];
            buf.get(alarmNoBytes);
            dto.setAlarmNo(new String(alarmNoBytes, GBK).trim());

            // 4. BYTE (1 byte): Information Type
            dto.setInformationType(Byte.toUnsignedInt(buf.get()));

            // 5. BYTE (1 byte): Attachment Count
            int attachmentCount = Byte.toUnsignedInt(buf.get());
            dto.setAttachmentCount(attachmentCount);

            // 6. Loop Attachment Information List
            List<AttachmentInformation> attachments = new ArrayList<>();
            for (int i = 0; i < attachmentCount; i++) {
                if (buf.hasRemaining()) {
                    AttachmentInformation info = new AttachmentInformation();

                    // BYTE: File Name Length
                    int fileNameLen = Byte.toUnsignedInt(buf.get());
                    info.setFileNameLength(fileNameLen);

                    // STRING: File Name
                    byte[] fileNameBytes = new byte[fileNameLen];
                    buf.get(fileNameBytes);
                    info.setFileName(new String(fileNameBytes, GBK).trim());

                    // DWORD: File Size
                    info.setFileSize(Integer.toUnsignedLong(buf.getInt()));

                    attachments.add(info);
                }
            }
            dto.setAttachmentInformationList(attachments);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Terminal ID    : %s", dto.getTerminalModelId());
            LOG.infof("Alarm No       : %s", dto.getAlarmNo());
            LOG.infof("Info Type      : %d", dto.getInformationType());
            LOG.infof("Attachments    : %d item(s)", attachments.size());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Alarm Attachments Information Message", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(AlarmAttachmentsInformationMessageDto dto) {
        try {
            List<AttachmentInformation> attachments = dto.getAttachmentInformationList() != null
                    ? dto.getAttachmentInformationList() : new ArrayList<>();

            // Hitung ukuran total buffer: 7 + 16 + 32 + 1 + 1 = 57 byte fixed length
            int totalSize = 57;
            for (AttachmentInformation info : attachments) {
                // 1 byte (length) + n byte (nama file) + 4 byte (size)
                byte[] fileNameBytes = info.getFileName() != null ? info.getFileName().getBytes(GBK) : new byte[0];
                totalSize += 1 + fileNameBytes.length + 4;
            }

            ByteBuffer buf = ByteBuffer.allocate(totalSize);

            // 1. BYTE[7]: Terminal Model ID (Zero-padded)
            byte[] terminalIdPadded = new byte[7];
            if (dto.getTerminalModelId() != null) {
                byte[] rawId = dto.getTerminalModelId().getBytes(GBK);
                System.arraycopy(rawId, 0, terminalIdPadded, 0, Math.min(rawId.length, 7));
            }
            buf.put(terminalIdPadded);

            // 2. BYTE[16]: Alarm Identification Number
            writeAlarmIdNumber(buf, dto.getAlarmIdentificationNumber());

            // 3. BYTE[32]: Alarm No. (Zero-padded)
            byte[] alarmNoPadded = new byte[32];
            if (dto.getAlarmNo() != null) {
                byte[] rawAlarmNo = dto.getAlarmNo().getBytes(GBK);
                System.arraycopy(rawAlarmNo, 0, alarmNoPadded, 0, Math.min(rawAlarmNo.length, 32));
            }
            buf.put(alarmNoPadded);

            // 4. BYTE: Information Type
            buf.put((byte) dto.getInformationType());

            // 5. BYTE: Attachment Count
            buf.put((byte) attachments.size());

            // 6. Attachment Information List
            for (AttachmentInformation info : attachments) {
                byte[] fileNameBytes = info.getFileName() != null ? info.getFileName().getBytes(GBK) : new byte[0];

                buf.put((byte) fileNameBytes.length); // File Name Length
                buf.put(fileNameBytes);               // File Name
                buf.putInt((int) info.getFileSize()); // File Size
            }

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Alarm Attachments Information Message", e);
            return new byte[0];
        }
    }

    // =======================================================================================
    // HELPER METHODS (Alarm ID & BCD)
    // =======================================================================================

    private AlarmIdentificationNumber parseAlarmIdNumber(ByteBuffer buf) {
        AlarmIdentificationNumber idNum = new AlarmIdentificationNumber();

        byte[] modelId = new byte[7];
        buf.get(modelId);
        idNum.setTerminalModelId(new String(modelId, GBK).trim());

        byte[] timeBcd = new byte[6];
        buf.get(timeBcd);
        idNum.setTime(decodeBcd(timeBcd));

        idNum.setIndex(Byte.toUnsignedInt(buf.get()));
        idNum.setAttachmentCount(Byte.toUnsignedInt(buf.get()));
        idNum.setReserved(Byte.toUnsignedInt(buf.get()));

        return idNum;
    }

    private void writeAlarmIdNumber(ByteBuffer buf, AlarmIdentificationNumber idNum) {
        if (idNum == null) idNum = new AlarmIdentificationNumber();

        byte[] modelId = new byte[7];
        if (idNum.getTerminalModelId() != null) {
            byte[] raw = idNum.getTerminalModelId().getBytes(GBK);
            System.arraycopy(raw, 0, modelId, 0, Math.min(raw.length, 7));
        }
        buf.put(modelId);

        buf.put(encodeBcd(idNum.getTime() != null ? idNum.getTime() : "000000000000"));
        buf.put((byte) idNum.getIndex());
        buf.put((byte) idNum.getAttachmentCount());
        buf.put((byte) idNum.getReserved());
    }

    private String decodeBcd(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private byte[] encodeBcd(String timeStr) {
        if (timeStr == null) timeStr = "";
        if (timeStr.length() < 12) {
            timeStr = String.format("%-12s", timeStr).replace(' ', '0');
        } else if (timeStr.length() > 12) {
            timeStr = timeStr.substring(0, 12);
        }

        byte[] bcd = new byte[6];
        for (int i = 0; i < 6; i++) {
            bcd[i] = (byte) Integer.parseInt(timeStr.substring(2 * i, 2 * i + 2), 16);
        }
        return bcd;
    }
}