package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.PlatformIssuedRemoteRecordingPlaybackRequestDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@ApplicationScoped
public class PlatformIssuedRemoteRecordingPlaybackRequestCodec implements Jtt808MessageCodec<PlatformIssuedRemoteRecordingPlaybackRequestDto> {

    private static final Logger LOG = Logger.getLogger(PlatformIssuedRemoteRecordingPlaybackRequestCodec.class);
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x9201; // ID Mesej untuk Platform Issued Remote Recording Playback Request
    }

    @Override
    public Class<PlatformIssuedRemoteRecordingPlaybackRequestDto> getSupportedDtoClass() {
        return PlatformIssuedRemoteRecordingPlaybackRequestDto.class;
    }

    @Override
    public String getCommandName() {
        return "Platform Issued Remote Recording Playback Request";
    }

    @Override
    public PlatformIssuedRemoteRecordingPlaybackRequestDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        PlatformIssuedRemoteRecordingPlaybackRequestDto dto = new PlatformIssuedRemoteRecordingPlaybackRequestDto();

        try {
            // 1. BYTE: IP Length
            int ipLen = Byte.toUnsignedInt(buf.get());
            dto.setServerIpLength(ipLen);

            // 2. STRING: Server IP
            byte[] ipBytes = new byte[ipLen];
            buf.get(ipBytes);
            dto.setServerIp(new String(ipBytes, GBK).trim());

            // 3. WORD: TCP Port
            dto.setTcpPort(Short.toUnsignedInt(buf.getShort()));

            // 4. WORD: UDP Port
            dto.setUdpPort(Short.toUnsignedInt(buf.getShort()));

            // 5. BYTE: Logical Channel
            dto.setLogicalChannelNumber(Byte.toUnsignedInt(buf.get()));

            // 6. BYTE: Audio and Video Type
            dto.setAudioAndVideoType(Byte.toUnsignedInt(buf.get()));

            // 7. BYTE: Stream Type
            dto.setStreamType(Byte.toUnsignedInt(buf.get()));

            // 8. BYTE: Storage Device Type
            dto.setStorageDeviceType(Byte.toUnsignedInt(buf.get()));

            // 9. BYTE: Playback Mode
            dto.setPlaybackMode(Byte.toUnsignedInt(buf.get()));

            // 10. BYTE: Multiplier
            dto.setFastForwardOrRewindMultiplier(Byte.toUnsignedInt(buf.get()));

            // 11. BCD[6]: Start Time
            byte[] startBcd = new byte[6];
            buf.get(startBcd);
            dto.setStartTime(decodeBcd(startBcd));

            // 12. BCD[6]: EndedTime
            byte[] endBcd = new byte[6];
            buf.get(endBcd);
            dto.setEndedTime(decodeBcd(endBcd));

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Server IP : %s (Len: %d)", dto.getServerIp(), dto.getServerIpLength());
            LOG.infof("Ports     : TCP %d | UDP %d", dto.getTcpPort(), dto.getUdpPort());
            LOG.infof("Channel   : %d | Playback Mode: %d | Speed: %d",
                    dto.getLogicalChannelNumber(), dto.getPlaybackMode(), dto.getFastForwardOrRewindMultiplier());
            LOG.infof("Waktu     : %s ke %s", dto.getStartTime(), dto.getEndedTime());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Platform Issued Remote Recording Playback Request", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(PlatformIssuedRemoteRecordingPlaybackRequestDto dto) {
        try {
            byte[] ipBytes = dto.getServerIp() != null ? dto.getServerIp().getBytes(GBK) : new byte[0];
            int ipLen = ipBytes.length;

            // Pengiraan saiz penimbal (buffer):
            // 1 (Len) + ipLen + 2 (TCP) + 2 (UDP) + 1 + 1 + 1 + 1 + 1 + 1 + 6 (Start) + 6 (End) = ipLen + 23 bytes
            ByteBuffer buf = ByteBuffer.allocate(23 + ipLen);

            // 1. IP Length
            buf.put((byte) ipLen);

            // 2. Server IP
            buf.put(ipBytes);

            // 3. TCP Port
            buf.putShort((short) dto.getTcpPort());

            // 4. UDP Port
            buf.putShort((short) dto.getUdpPort());

            // 5. Channel
            buf.put((byte) dto.getLogicalChannelNumber());

            // 6. A/V Type
            buf.put((byte) dto.getAudioAndVideoType());

            // 7. Stream Type
            buf.put((byte) dto.getStreamType());

            // 8. Storage Device
            buf.put((byte) dto.getStorageDeviceType());

            // 9. Playback Mode
            buf.put((byte) dto.getPlaybackMode());

            // 10. Multiplier
            buf.put((byte) dto.getFastForwardOrRewindMultiplier());

            // 11. Start Time
            buf.put(encodeBcd(dto.getStartTime()));

            // 12. EndedTime
            buf.put(encodeBcd(dto.getEndedTime()));

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Platform Issued Remote Recording Playback Request", e);
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