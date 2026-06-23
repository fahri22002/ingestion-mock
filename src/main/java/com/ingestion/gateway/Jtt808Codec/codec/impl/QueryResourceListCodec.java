package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.QueryResourceListDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;

@ApplicationScoped
public class QueryResourceListCodec implements Jtt808MessageCodec<QueryResourceListDto> {

    private static final Logger LOG = Logger.getLogger(QueryResourceListCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x9205; // ID Mesej untuk Query Resource List
    }

    @Override
    public Class<QueryResourceListDto> getSupportedDtoClass() {
        return QueryResourceListDto.class;
    }

    @Override
    public String getCommandName() {
        return "Query Resource List";
    }

    @Override
    public QueryResourceListDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        QueryResourceListDto dto = new QueryResourceListDto();

        try {
            // 1. BYTE (1 byte): Logical Channel Number
            dto.setLogicalChannelNumber(Byte.toUnsignedInt(buf.get()));

            // 2. BCD[6] (6 byte): Start Time
            byte[] startTimeBcd = new byte[6];
            buf.get(startTimeBcd);
            dto.setStartTime(decodeBcd(startTimeBcd));

            // 3. BCD[6] (6 byte): EndedTime
            byte[] endedTimeBcd = new byte[6];
            buf.get(endedTimeBcd);
            dto.setEndedTime(decodeBcd(endedTimeBcd));

            // 4. 64BITS / DWORD x 2 (8 byte): Alarm Flag
            dto.setAlarmFlag(buf.getLong());

            // 5. BYTE (1 byte): Audio and Video Resource Type
            dto.setAudioAndVideoResourceType(Byte.toUnsignedInt(buf.get()));

            // 6. BYTE (1 byte): Stream type
            dto.setStreamType(Byte.toUnsignedInt(buf.get()));

            // 7. BYTE (1 byte): Storage Device Type
            dto.setStorageDeviceType(Byte.toUnsignedInt(buf.get()));

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Channel : %d", dto.getLogicalChannelNumber());
            LOG.infof("Start   : %s", dto.getStartTime());
            LOG.infof("End     : %s", dto.getEndedTime());
            LOG.infof("A/V Type: %d | Stream: %d | Storage: %d",
                    dto.getAudioAndVideoResourceType(), dto.getStreamType(), dto.getStorageDeviceType());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Query Resource List", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(QueryResourceListDto dto) {
        try {
            // Total panjang konstan sesuai Tabel 73: 1 + 6 + 6 + 8 + 1 + 1 + 1 = 24 bytes
            ByteBuffer buf = ByteBuffer.allocate(24);

            // 1. BYTE: Logical Channel Number
            buf.put((byte) dto.getLogicalChannelNumber());

            // 2. BCD[6]: Start Time
            buf.put(encodeBcd(dto.getStartTime() != null ? dto.getStartTime() : "000000000000"));

            // 3. BCD[6]: EndedTime
            buf.put(encodeBcd(dto.getEndedTime() != null ? dto.getEndedTime() : "000000000000"));

            // 4. 64BITS (8 byte): Alarm Flag
            buf.putLong(dto.getAlarmFlag());

            // 5. BYTE: Audio and Video Resource Type
            buf.put((byte) dto.getAudioAndVideoResourceType());

            // 6. BYTE: Stream type
            buf.put((byte) dto.getStreamType());

            // 7. BYTE: Storage Device Type
            buf.put((byte) dto.getStorageDeviceType());

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Query Resource List", e);
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

        // Memastikan panjang string 12 karakter sebelum parsing
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