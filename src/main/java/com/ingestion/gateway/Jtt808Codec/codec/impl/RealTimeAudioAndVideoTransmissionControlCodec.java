package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.RealTimeAudioAndVideoTransmissionControlDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;

@ApplicationScoped
public class RealTimeAudioAndVideoTransmissionControlCodec implements Jtt808MessageCodec<RealTimeAudioAndVideoTransmissionControlDto> {

    private static final Logger LOG = Logger.getLogger(RealTimeAudioAndVideoTransmissionControlCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x9102; // ID Mesej untuk Real-time Audio and Video Transmission Control
    }

    @Override
    public Class<RealTimeAudioAndVideoTransmissionControlDto> getSupportedDtoClass() {
        return RealTimeAudioAndVideoTransmissionControlDto.class;
    }

    @Override
    public String getCommandName() {
        return "Real-time Audio and Video Transmission Control";
    }

    @Override
    public RealTimeAudioAndVideoTransmissionControlDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        RealTimeAudioAndVideoTransmissionControlDto dto = new RealTimeAudioAndVideoTransmissionControlDto();

        try {
            // 1. BYTE (1 byte): Logical Channel Number
            dto.setLogicalChannelNumber(Byte.toUnsignedInt(buf.get()));

            // 2. BYTE (1 byte): Control Command
            dto.setControlCommand(Byte.toUnsignedInt(buf.get()));

            // 3. BYTE (1 byte): Turn Off Audio and Video Type
            dto.setTurnOffAudioAndVideoType(Byte.toUnsignedInt(buf.get()));

            // 4. BYTE (1 byte): Switch Stream Type
            dto.setSwitchStreamType(Byte.toUnsignedInt(buf.get()));

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Logical Channel : %d", dto.getLogicalChannelNumber());
            LOG.infof("Control Command : %d", dto.getControlCommand());
            LOG.infof("Turn Off Type   : %d", dto.getTurnOffAudioAndVideoType());
            LOG.infof("Switch Stream   : %d", dto.getSwitchStreamType());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Real-time Audio and Video Transmission Control", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(RealTimeAudioAndVideoTransmissionControlDto dto) {
        try {
            // Total Alokasi Buffer: 4 byte konstan
            ByteBuffer buf = ByteBuffer.allocate(4);

            // 1. BYTE: Logical Channel Number
            buf.put((byte) dto.getLogicalChannelNumber());

            // 2. BYTE: Control Command
            buf.put((byte) dto.getControlCommand());

            // 3. BYTE: Turn Off Audio and Video Type
            buf.put((byte) dto.getTurnOffAudioAndVideoType());

            // 4. BYTE: Switch Stream Type
            buf.put((byte) dto.getSwitchStreamType());

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Real-time Audio and Video Transmission Control", e);
            return new byte[0];
        }
    }
}