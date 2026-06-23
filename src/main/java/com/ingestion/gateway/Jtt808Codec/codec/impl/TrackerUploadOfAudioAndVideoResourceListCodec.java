package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.TrackerUploadOfAudioAndVideoResourceListDto;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.TrackerUploadOfAudioAndVideoResourceListDto.ResourceItem;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TrackerUploadOfAudioAndVideoResourceListCodec implements Jtt808MessageCodec<TrackerUploadOfAudioAndVideoResourceListDto> {

    private static final Logger LOG = Logger.getLogger(TrackerUploadOfAudioAndVideoResourceListCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x1205; // ID Mesej untuk Tracker Upload of Audio and Video Resource List
    }

    @Override
    public Class<TrackerUploadOfAudioAndVideoResourceListDto> getSupportedDtoClass() {
        return TrackerUploadOfAudioAndVideoResourceListDto.class;
    }

    @Override
    public String getCommandName() {
        return "Tracker Upload of Audio and Video Resource List";
    }

    @Override
    public TrackerUploadOfAudioAndVideoResourceListDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        TrackerUploadOfAudioAndVideoResourceListDto dto = new TrackerUploadOfAudioAndVideoResourceListDto();

        try {
            // 1. WORD (2 byte): Serial Number
            dto.setResponseSerialNumber(Short.toUnsignedInt(buf.getShort()));

            // 2. DWORD (4 byte): Total Number of Resources
            long totalResources = Integer.toUnsignedLong(buf.getInt());
            dto.setTotalResources(totalResources);

            // 3. Loop mengekstrak item sumber (Setiap item = 28 byte)
            List<ResourceItem> items = new ArrayList<>();
            for (long i = 0; i < totalResources; i++) {
                if (buf.remaining() >= 28) {
                    ResourceItem item = new ResourceItem();

                    // BYTE: Logical Channel Number
                    item.setLogicalChannelNumber(Byte.toUnsignedInt(buf.get()));

                    // BCD[6]: Start Time
                    byte[] startBcd = new byte[6];
                    buf.get(startBcd);
                    item.setStartTime(decodeBcd(startBcd));

                    // BCD[6]: EndedTime
                    byte[] endBcd = new byte[6];
                    buf.get(endBcd);
                    item.setEndedTime(decodeBcd(endBcd));

                    // 64BITS: Alarm Flag
                    item.setAlarmFlag(buf.getLong());

                    // BYTE: Audio and Video Resource Type
                    item.setAudioAndVideoResourceType(Byte.toUnsignedInt(buf.get()));

                    // BYTE: Stream type
                    item.setStreamType(Byte.toUnsignedInt(buf.get()));

                    // BYTE: Storage Device Type
                    item.setStorageDeviceType(Byte.toUnsignedInt(buf.get()));

                    // DWORD: File Size
                    item.setFileSize(Integer.toUnsignedLong(buf.getInt()));

                    items.add(item);
                } else {
                    LOG.warnf("Buffer tidak mencukupi untuk item ke-%d. Dijangka: 28, Sisa: %d", i + 1, buf.remaining());
                    break;
                }
            }
            dto.setResourceList(items);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Serial Num : %d", dto.getResponseSerialNumber());
            LOG.infof("Total Items: %d (Actual List Size: %d)", dto.getTotalResources(), items.size());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Tracker Upload of Audio and Video Resource List", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(TrackerUploadOfAudioAndVideoResourceListDto dto) {
        try {
            List<ResourceItem> items = dto.getResourceList() != null ? dto.getResourceList() : new ArrayList<>();
            int itemCount = items.size();

            // Total Alokasi Buffer: 2 (Serial) + 4 (Total) + (28 * jumlah item)
            int totalSize = 2 + 4 + (28 * itemCount);
            ByteBuffer buf = ByteBuffer.allocate(totalSize);

            // 1. WORD: Serial Number
            buf.putShort((short) dto.getResponseSerialNumber());

            // 2. DWORD: Total Number of Resources
            buf.putInt(itemCount); // Sentiasa gunakan saiz senarai sebenar

            // 3. Resource Item List
            for (ResourceItem item : items) {
                // BYTE: Logical Channel Number
                buf.put((byte) item.getLogicalChannelNumber());

                // BCD[6]: Start Time
                buf.put(encodeBcd(item.getStartTime()));

                // BCD[6]: EndedTime
                buf.put(encodeBcd(item.getEndedTime()));

                // 64BITS: Alarm Flag
                buf.putLong(item.getAlarmFlag());

                // BYTE: Resource Type
                buf.put((byte) item.getAudioAndVideoResourceType());

                // BYTE: Stream Type
                buf.put((byte) item.getStreamType());

                // BYTE: Storage Device Type
                buf.put((byte) item.getStorageDeviceType());

                // DWORD: File Size
                buf.putInt((int) item.getFileSize());
            }

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Tracker Upload of Audio and Video Resource List", e);
            return new byte[0];
        }
    }

    // =======================================================================================
    // HELPER METHODS (BCD)
    // =======================================================================================

    private String decodeBcd(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private byte[] encodeBcd(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            timeStr = "000000000000";
        }

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