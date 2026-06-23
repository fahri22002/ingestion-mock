package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.AlarmAttachmentsUploadCommandDto;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.AlarmAttachmentsUploadCommandDto.AlarmIdentificationNumber;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@ApplicationScoped
public class AlarmAttachmentsUploadCommandCodec implements Jtt808MessageCodec<AlarmAttachmentsUploadCommandDto> {

    private static final Logger LOG = Logger.getLogger(AlarmAttachmentsUploadCommandCodec.class);
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x9208; // ID Mesej untuk Alarm Attachments Upload Command
    }

    @Override
    public Class<AlarmAttachmentsUploadCommandDto> getSupportedDtoClass() {
        return AlarmAttachmentsUploadCommandDto.class;
    }

    @Override
    public String getCommandName() {
        return "Alarm Attachments Upload Command";
    }

    @Override
    public AlarmAttachmentsUploadCommandDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        AlarmAttachmentsUploadCommandDto dto = new AlarmAttachmentsUploadCommandDto();

        try {
            // 1. Ambil Server IP Address Length (BYTE)
            int ipLen = Byte.toUnsignedInt(buf.get());
            dto.setServerIpLength(ipLen);

            // 2. Ambil Server IP Address (STRING)
            byte[] ipBytes = new byte[ipLen];
            buf.get(ipBytes);
            dto.setServerIp(new String(ipBytes, GBK).trim());

            // 3. Ambil TCP & UDP Port (WORD x 2)
            dto.setTcpPort(Short.toUnsignedInt(buf.getShort()));
            dto.setUdpPort(Short.toUnsignedInt(buf.getShort()));

            // 4. Ambil Alarm Identification Number (BYTE[16])
            dto.setAlarmIdentificationNumber(parseAlarmIdNumber(buf));

            // 5. Ambil Alarm No. (BYTE[32])
            byte[] alarmNoBytes = new byte[32];
            buf.get(alarmNoBytes);
            dto.setAlarmNo(new String(alarmNoBytes, GBK).trim());

            // 6. Ambil Reserved (BYTE[16])
            byte[] reservedBytes = new byte[16];
            buf.get(reservedBytes);
            dto.setReserved(reservedBytes);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Server IP      : %s", dto.getServerIp());
            LOG.infof("TCP Port       : %d | UDP Port: %d", dto.getTcpPort(), dto.getUdpPort());
            LOG.infof("Alarm No       : %s", dto.getAlarmNo());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Alarm Attachments Upload Command", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(AlarmAttachmentsUploadCommandDto dto) {
        try {
            byte[] ipBytes = dto.getServerIp() != null ? dto.getServerIp().getBytes(GBK) : new byte[0];
            int ipLen = ipBytes.length;

            // Alokasi: 1 (IP Len) + ipLen + 2 (TCP) + 2 (UDP) + 16 (Alarm ID) + 32 (Alarm No) + 16 (Reserved)
            ByteBuffer buf = ByteBuffer.allocate(1 + ipLen + 2 + 2 + 16 + 32 + 16);

            // 1. Tulis IP Length & IP Address
            buf.put((byte) ipLen);
            buf.put(ipBytes);

            // 2. Tulis TCP & UDP Port
            buf.putShort((short) dto.getTcpPort());
            buf.putShort((short) dto.getUdpPort());

            // 3. Tulis Alarm Identification Number (16 byte)
            writeAlarmIdNumber(buf, dto.getAlarmIdentificationNumber());

            // 4. Tulis Alarm No (32 byte)
            byte[] alarmNoPadded = new byte[32];
            if (dto.getAlarmNo() != null) {
                byte[] rawAlarmNo = dto.getAlarmNo().getBytes(GBK);
                System.arraycopy(rawAlarmNo, 0, alarmNoPadded, 0, Math.min(rawAlarmNo.length, 32));
            }
            buf.put(alarmNoPadded);

            // 5. Tulis Reserved (16 byte)
            byte[] reserved = dto.getReserved() != null && dto.getReserved().length == 16 ? dto.getReserved() : new byte[16];
            buf.put(reserved);

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Alarm Attachments Upload Command", e);
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