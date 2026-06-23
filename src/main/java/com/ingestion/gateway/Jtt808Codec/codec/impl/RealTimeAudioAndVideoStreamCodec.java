package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.dto.impl.core.RealTimeAudioAndVideoStreamDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import java.nio.ByteBuffer;

@ApplicationScoped
public class RealTimeAudioAndVideoStreamCodec {

    private static final Logger LOG = Logger.getLogger(RealTimeAudioAndVideoStreamCodec.class);
    public static final long FRAME_HEADER_MAGIC = 0x30316364L;

    public RealTimeAudioAndVideoStreamDto decodeStream(byte[] rawStreamData) {
        ByteBuffer buf = ByteBuffer.wrap(rawStreamData);
        RealTimeAudioAndVideoStreamDto dto = new RealTimeAudioAndVideoStreamDto();
        try {
            dto.setFrameHeaderIdentifier(Integer.toUnsignedLong(buf.getInt()));
            byte b4 = buf.get();
            dto.setV((b4 >> 6) & 0x03);
            dto.setP((b4 >> 5) & 0x01);
            dto.setX((b4 >> 4) & 0x01);
            dto.setCc(b4 & 0x0F);
            byte b5 = buf.get();
            dto.setM((b5 >> 7) & 0x01);
            dto.setPt(b5 & 0x7F);
            dto.setPacketSequenceNumber(Short.toUnsignedInt(buf.getShort()));
            byte[] simBytes = new byte[6];
            buf.get(simBytes);
            dto.setSimCardNumber(decodeBcd(simBytes));
            dto.setLogicalChannelNumber(Byte.toUnsignedInt(buf.get()));
            byte b15 = buf.get();
            dto.setDataType((b15 >> 4) & 0x0F);
            dto.setFragmentationFlag(b15 & 0x0F);
            if (dto.getDataType() != 4) {
                dto.setTimestamp(buf.getLong());
                if (dto.getDataType() == 0 || dto.getDataType() == 1 || dto.getDataType() == 2) {
                    dto.setLastIFrameInterval(Short.toUnsignedInt(buf.getShort()));
                    dto.setLastFrameInterval(Short.toUnsignedInt(buf.getShort()));
                }
            }
            dto.setDataBodyLength(Short.toUnsignedInt(buf.getShort()));
            byte[] body = new byte[dto.getDataBodyLength()];
            buf.get(body);
            dto.setDataBody(body);
        } catch (Exception e) {
            LOG.error("Gagal decode Real-time A/V Stream", e);
            return null;
        }
        return dto;
    }

    public byte[] encodeStream(RealTimeAudioAndVideoStreamDto dto) {
        byte[] body = dto.getDataBody() != null ? dto.getDataBody() : new byte[0];

        // 1. Hitung headerSize secara akurat berdasarkan spesifikasi tabel
        // Base (16) + Len(2) = 18 byte (untuk Data Type 4)
        int headerSize = 18;

        if (dto.getDataType() != 4) {
            headerSize += 8; // Tambah Timestamp
            if (dto.getDataType() <= 2) {
                headerSize += 4; // Tambah I Interval (2) + F Interval (2)
            }
        }

        // 2. Alokasikan buffer dengan total ukuran yang presisi
        ByteBuffer buf = ByteBuffer.allocate(headerSize + body.length);

        // 3. Masukkan data ke buffer sesuai urutan tabel
        buf.putInt((int) FRAME_HEADER_MAGIC);

        // V(2), P(1), X(1), CC(4)
        buf.put((byte) ((dto.getV() << 6) | (dto.getP() << 5) | (dto.getX() << 4) | (dto.getCc() & 0x0F)));

        // M(1), PT(7)
        buf.put((byte) ((dto.getM() << 7) | (dto.getPt() & 0x7F)));

        buf.putShort((short) dto.getPacketSequenceNumber());
        buf.put(encodeBcd(dto.getSimCardNumber()));
        buf.put((byte) dto.getLogicalChannelNumber());

        // Data Type(4), Fragmentation(4)
        buf.put((byte) ((dto.getDataType() << 4) | (dto.getFragmentationFlag() & 0x0F)));

        // Field opsional
        if (dto.getDataType() != 4) {
            buf.putLong(dto.getTimestamp());
            if (dto.getDataType() <= 2) {
                buf.putShort((short) dto.getLastIFrameInterval());
                buf.putShort((short) dto.getLastFrameInterval());
            }
        }

        // Akhiri dengan panjang body dan data bodynya
        buf.putShort((short) body.length);
        buf.put(body);

        return buf.array();
    }

    private String decodeBcd(byte[] b) { StringBuilder sb = new StringBuilder(); for(byte x : b) sb.append(String.format("%02X", x)); return sb.toString(); }
    private byte[] encodeBcd(String s) { byte[] b = new byte[6]; String p = String.format("%-12s", s).replace(' ', '0'); for(int i=0; i<6; i++) b[i] = (byte)Integer.parseInt(p.substring(2*i, 2*i+2), 16); return b; }
}