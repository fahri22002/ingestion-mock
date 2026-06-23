package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.RealTimeAudioAndVideoTransmissionRequestDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@ApplicationScoped
public class RealTimeAudioAndVideoTransmissionRequestCodec implements Jtt808MessageCodec<RealTimeAudioAndVideoTransmissionRequestDto> {

    private static final Logger LOG = Logger.getLogger(RealTimeAudioAndVideoTransmissionRequestCodec.class);

    // Standar protokol JTT808 menggunakan GBK untuk STRING
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x9101; // ID Mesej untuk Real-time Audio and Video Transmission Request
    }

    @Override
    public Class<RealTimeAudioAndVideoTransmissionRequestDto> getSupportedDtoClass() {
        return RealTimeAudioAndVideoTransmissionRequestDto.class;
    }

    @Override
    public String getCommandName() {
        return "Real-time Audio and Video Transmission Request";
    }

    @Override
    public RealTimeAudioAndVideoTransmissionRequestDto decodeBody(byte[] bodyData) {
        ByteBuffer buf = ByteBuffer.wrap(bodyData);
        RealTimeAudioAndVideoTransmissionRequestDto dto = new RealTimeAudioAndVideoTransmissionRequestDto();

        try {
            // 1. BYTE (1 byte): Server IP Address Length
            int ipLen = Byte.toUnsignedInt(buf.get());
            dto.setServerIpLength(ipLen);

            // 2. STRING (n bytes): Server IP Address
            byte[] ipBytes = new byte[ipLen];
            buf.get(ipBytes);
            dto.setServerIp(new String(ipBytes, GBK).trim());

            // 3. WORD (2 byte): TCP Port
            dto.setTcpPort(Short.toUnsignedInt(buf.getShort()));

            // 4. WORD (2 byte): UDP Port
            dto.setUdpPort(Short.toUnsignedInt(buf.getShort()));

            // 5. BYTE (1 byte): Logical Channel Number
            dto.setLogicalChannelNumber(Byte.toUnsignedInt(buf.get()));

            // 6. BYTE (1 byte): Data Type
            dto.setDataType(Byte.toUnsignedInt(buf.get()));

            // 7. BYTE (1 byte): Stream Type
            dto.setStreamType(Byte.toUnsignedInt(buf.get()));

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Server IP       : %s (Len: %d)", dto.getServerIp(), dto.getServerIpLength());
            LOG.infof("TCP Port        : %d | UDP Port: %d", dto.getTcpPort(), dto.getUdpPort());
            LOG.infof("Logical Channel : %d", dto.getLogicalChannelNumber());
            LOG.infof("Data Type       : %d", dto.getDataType());
            LOG.infof("Stream Type     : %d", dto.getStreamType());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Real-time Audio and Video Transmission Request", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(RealTimeAudioAndVideoTransmissionRequestDto dto) {
        try {
            byte[] ipBytes = dto.getServerIp() != null ? dto.getServerIp().getBytes(GBK) : new byte[0];
            int ipLen = ipBytes.length;

            // Total Alokasi Buffer:
            // 1 (IP Len) + ipLen + 2 (TCP) + 2 (UDP) + 1 (Channel) + 1 (Data Type) + 1 (Stream Type) = 8 + ipLen byte
            ByteBuffer buf = ByteBuffer.allocate(8 + ipLen);

            // 1. BYTE: Server IP Address Length
            buf.put((byte) ipLen);

            // 2. STRING: Server IP Address
            buf.put(ipBytes);

            // 3. WORD: TCP Port
            buf.putShort((short) dto.getTcpPort());

            // 4. WORD: UDP Port
            buf.putShort((short) dto.getUdpPort());

            // 5. BYTE: Logical Channel Number
            buf.put((byte) dto.getLogicalChannelNumber());

            // 6. BYTE: Data Type
            buf.put((byte) dto.getDataType());

            // 7. BYTE: Stream Type
            buf.put((byte) dto.getStreamType());

            return buf.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Real-time Audio and Video Transmission Request", e);
            return new byte[0];
        }
    }
}