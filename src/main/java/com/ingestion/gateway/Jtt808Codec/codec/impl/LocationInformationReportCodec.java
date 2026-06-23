package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.LocationInformationReportDto;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.LocationInformationReportDto.*;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class LocationInformationReportCodec implements Jtt808MessageCodec<LocationInformationReportDto> {

    private static final Logger LOG = Logger.getLogger(LocationInformationReportCodec.class);
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x0200; // ID Perintah Location Information Report
    }

    @Override
    public Class<LocationInformationReportDto> getSupportedDtoClass() {
        return LocationInformationReportDto.class;
    }

    @Override
    public String getCommandName() {
        return "Location Information Report";
    }

    @Override
    public LocationInformationReportDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        LocationInformationReportDto dto = new LocationInformationReportDto();

        try {
            // 1. Ekstrak Basic Location Information (28 Bytes)
            dto.setAlarmFlag(Integer.toUnsignedLong(buf.getInt()));
            dto.setStatus(Integer.toUnsignedLong(buf.getInt()));
            dto.setLatitude(Integer.toUnsignedLong(buf.getInt()));
            dto.setLongitude(Integer.toUnsignedLong(buf.getInt()));
            dto.setElevation(Short.toUnsignedInt(buf.getShort()));
            dto.setSpeed(Short.toUnsignedInt(buf.getShort()));
            dto.setDirection(Short.toUnsignedInt(buf.getShort()));

            byte[] timeBcd = new byte[6];
            buf.get(timeBcd);
            dto.setTime(decodeBcd(timeBcd));

            // 2. Ekstrak Additional Location Information List
            List<AdditionalItem> items = new ArrayList<>();
            while (buf.hasRemaining()) {
                int id = Byte.toUnsignedInt(buf.get());
                int length = Byte.toUnsignedInt(buf.get());
                byte[] rawValue = new byte[length];
                buf.get(rawValue);

                AdditionalItem item = new AdditionalItem();
                item.setId(id);
                item.setLength(length);
                item.setRawValue(rawValue);

                // Parsing sub-data spesifik jika ID dikenali dan panjang sesuai (47 byte untuk ADAS/DSM)
                if (id == 0x64 && length == 47) {
                    item.setAdasAlarmInfo(parseAdasAlarmInfo(rawValue));
                } else if (id == 0x65 && length == 47) {
                    item.setDsmAlarmInfo(parseDsmAlarmInfo(rawValue));
                }
                items.add(item);
            }
            dto.setAdditionalItems(items);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Lat: %d | Lon: %d | Speed: %d", dto.getLatitude(), dto.getLongitude(), dto.getSpeed());
            LOG.infof("Total Additional Items: %d", items.size());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Location Information Report", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(LocationInformationReportDto dto) {
        try {
            // Hitung ukuran total payload dinamis
            int totalSize = 28; // Basic size konstan
            List<AdditionalItem> items = dto.getAdditionalItems() != null ? dto.getAdditionalItems() : new ArrayList<>();

            for (AdditionalItem item : items) {
                // Memastikan data raw diperbarui jika object spesifiknya ada perubahan sebelum proses encode
                if (item.getId() == 0x64 && item.getAdasAlarmInfo() != null) {
                    item.setRawValue(encodeAdasAlarmInfo(item.getAdasAlarmInfo()));
                    item.setLength(item.getRawValue().length);
                } else if (item.getId() == 0x65 && item.getDsmAlarmInfo() != null) {
                    item.setRawValue(encodeDsmAlarmInfo(item.getDsmAlarmInfo()));
                    item.setLength(item.getRawValue().length);
                }
                totalSize += 2 + item.getLength(); // 1 byte ID + 1 byte Length + Value
            }

            ByteBuffer buf = ByteBuffer.allocate(totalSize);

            // 1. Tulis Basic Location Information
            buf.putInt((int) dto.getAlarmFlag());
            buf.putInt((int) dto.getStatus());
            buf.putInt((int) dto.getLatitude());
            buf.putInt((int) dto.getLongitude());
            buf.putShort((short) dto.getElevation());
            buf.putShort((short) dto.getSpeed());
            buf.putShort((short) dto.getDirection());
            buf.put(encodeBcd(dto.getTime() != null ? dto.getTime() : "000000000000"));

            // 2. Tulis Additional Location Information List
            for (AdditionalItem item : items) {
                buf.put((byte) item.getId());
                buf.put((byte) item.getLength());
                buf.put(item.getRawValue());
            }

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Location Information Report", e);
            return new byte[0];
        }
    }

    // =======================================================================================
    // HELPER METHODS UNTUK PARSING ADAS (0x64) & DSM (0x65)
    // =======================================================================================

    private AdasAlarmInfo parseAdasAlarmInfo(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        AdasAlarmInfo info = new AdasAlarmInfo();
        info.setAlarmId(Integer.toUnsignedLong(buf.getInt()));
        info.setFlagStatus(Byte.toUnsignedInt(buf.get()));
        info.setAlarmEventType(Byte.toUnsignedInt(buf.get()));
        info.setAlarmLevel(Byte.toUnsignedInt(buf.get()));
        info.setPrecedingVehicleSpeed(Byte.toUnsignedInt(buf.get()));
        info.setDistanceToPreceding(Byte.toUnsignedInt(buf.get()));
        info.setDeviationType(Byte.toUnsignedInt(buf.get()));
        info.setRoadSignType(Byte.toUnsignedInt(buf.get()));
        info.setRoadSignData(Byte.toUnsignedInt(buf.get()));
        info.setVehicleSpeed(Byte.toUnsignedInt(buf.get()));
        info.setElevation(Short.toUnsignedInt(buf.getShort()));
        info.setLatitude(Integer.toUnsignedLong(buf.getInt()));
        info.setLongitude(Integer.toUnsignedLong(buf.getInt()));
        byte[] timeBcd = new byte[6]; buf.get(timeBcd);
        info.setDateTime(decodeBcd(timeBcd));
        info.setVehicleStatus(Short.toUnsignedInt(buf.getShort()));
        info.setAlarmIdentificationNumber(parseAlarmIdNumber(buf));
        return info;
    }

    private DsmAlarmInfo parseDsmAlarmInfo(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        DsmAlarmInfo info = new DsmAlarmInfo();
        info.setAlarmId(Integer.toUnsignedLong(buf.getInt()));
        info.setFlagStatus(Byte.toUnsignedInt(buf.get()));
        info.setAlarmEventType(Byte.toUnsignedInt(buf.get()));
        info.setAlarmLevel(Byte.toUnsignedInt(buf.get()));
        info.setDrowsinessLevel(Byte.toUnsignedInt(buf.get()));
        byte[] reserved = new byte[4]; buf.get(reserved);
        info.setReserved(reserved);
        info.setVehicleSpeed(Byte.toUnsignedInt(buf.get()));
        info.setElevation(Short.toUnsignedInt(buf.getShort()));
        info.setLatitude(Integer.toUnsignedLong(buf.getInt()));
        info.setLongitude(Integer.toUnsignedLong(buf.getInt()));
        byte[] timeBcd = new byte[6]; buf.get(timeBcd);
        info.setDateTime(decodeBcd(timeBcd));
        info.setVehicleStatus(Short.toUnsignedInt(buf.getShort()));
        info.setAlarmIdentificationNumber(parseAlarmIdNumber(buf));
        return info;
    }

    private AlarmIdentificationNumber parseAlarmIdNumber(ByteBuffer buf) {
        AlarmIdentificationNumber idNum = new AlarmIdentificationNumber();
        byte[] modelId = new byte[7]; buf.get(modelId);
        idNum.setTerminalModelId(new String(modelId, GBK).trim());
        byte[] timeBcd = new byte[6]; buf.get(timeBcd);
        idNum.setTime(decodeBcd(timeBcd));
        idNum.setIndex(Byte.toUnsignedInt(buf.get()));
        idNum.setAttachmentCount(Byte.toUnsignedInt(buf.get()));
        idNum.setReserved(Byte.toUnsignedInt(buf.get()));
        return idNum;
    }

    private byte[] encodeAdasAlarmInfo(AdasAlarmInfo info) {
        ByteBuffer buf = ByteBuffer.allocate(47);
        buf.putInt((int) info.getAlarmId());
        buf.put((byte) info.getFlagStatus());
        buf.put((byte) info.getAlarmEventType());
        buf.put((byte) info.getAlarmLevel());
        buf.put((byte) info.getPrecedingVehicleSpeed());
        buf.put((byte) info.getDistanceToPreceding());
        buf.put((byte) info.getDeviationType());
        buf.put((byte) info.getRoadSignType());
        buf.put((byte) info.getRoadSignData());
        buf.put((byte) info.getVehicleSpeed());
        buf.putShort((short) info.getElevation());
        buf.putInt((int) info.getLatitude());
        buf.putInt((int) info.getLongitude());
        buf.put(encodeBcd(info.getDateTime() != null ? info.getDateTime() : "000000000000"));
        buf.putShort((short) info.getVehicleStatus());
        writeAlarmIdNumber(buf, info.getAlarmIdentificationNumber());
        return buf.array();
    }

    private byte[] encodeDsmAlarmInfo(DsmAlarmInfo info) {
        ByteBuffer buf = ByteBuffer.allocate(47);
        buf.putInt((int) info.getAlarmId());
        buf.put((byte) info.getFlagStatus());
        buf.put((byte) info.getAlarmEventType());
        buf.put((byte) info.getAlarmLevel());
        buf.put((byte) info.getDrowsinessLevel());
        buf.put(info.getReserved() != null && info.getReserved().length == 4 ? info.getReserved() : new byte[4]);
        buf.put((byte) info.getVehicleSpeed());
        buf.putShort((short) info.getElevation());
        buf.putInt((int) info.getLatitude());
        buf.putInt((int) info.getLongitude());
        buf.put(encodeBcd(info.getDateTime() != null ? info.getDateTime() : "000000000000"));
        buf.putShort((short) info.getVehicleStatus());
        writeAlarmIdNumber(buf, info.getAlarmIdentificationNumber());
        return buf.array();
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

    // =======================================================================================
    // BCD UTILS
    // =======================================================================================

    private String decodeBcd(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private byte[] encodeBcd(String timeStr) {
        // Normalisasi format panjang agar tidak out of bounds
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