package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;

@ApplicationScoped
public class RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformCodec implements Jtt808MessageCodec<RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto> {

    private static final Logger LOG = Logger.getLogger(RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x9202; // ID Mesej untuk Remote Audio and Video Recording Playback Control Command Issued by Platform
    }

    @Override
    public Class<RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto> getSupportedDtoClass() {
        return RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto.class;
    }

    @Override
    public String getCommandName() {
        return "Remote Audio and Video Recording Playback Control Command Issued by Platform";
    }

    @Override
    public RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto dto = new RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto();

        try {
            // 1. BYTE (1 byte): Audio and Video Channel Number
            dto.setAudioAndVideoChannelNumber(Byte.toUnsignedInt(buf.get()));

            // 2. BYTE (1 byte): Playback Control
            dto.setPlaybackControl(Byte.toUnsignedInt(buf.get()));

            // 3. BYTE (1 byte): Fast Forward or Rewind Multiplier
            dto.setFastForwardOrRewindMultiplier(Byte.toUnsignedInt(buf.get()));

            // 4. BCD[6] (6 byte): Drag Playback Position
            byte[] positionBcd = new byte[6];
            buf.get(positionBcd);
            dto.setDragPlaybackPosition(decodeBcd(positionBcd));

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Channel Number : %d", dto.getAudioAndVideoChannelNumber());
            LOG.infof("Control Type   : %d", dto.getPlaybackControl());
            LOG.infof("Multiplier     : %d", dto.getFastForwardOrRewindMultiplier());
            LOG.infof("Drag Position  : %s", dto.getDragPlaybackPosition());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Remote Audio and Video Recording Playback Control Command Issued by Platform", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto dto) {
        try {
            // Total alokasi buffer konstan: 1 + 1 + 1 + 6 = 9 byte
            ByteBuffer buf = ByteBuffer.allocate(9);

            // 1. BYTE: Audio and Video Channel Number
            buf.put((byte) dto.getAudioAndVideoChannelNumber());

            // 2. BYTE: Playback Control
            buf.put((byte) dto.getPlaybackControl());

            // 3. BYTE: Fast Forward or Rewind Multiplier
            buf.put((byte) dto.getFastForwardOrRewindMultiplier());

            // 4. BCD[6]: Drag Playback Position
            buf.put(encodeBcd(dto.getDragPlaybackPosition()));

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Remote Audio and Video Recording Playback Control Command Issued by Platform", e);
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