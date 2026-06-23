// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.AlarmVideoInfoACodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.AlarmVideoInfoDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class AlarmVideoInfoACodec extends AbstractByteUnfixedCodec<AlarmVideoInfoDto> {

    private static final Logger LOG = Logger.getLogger(AlarmVideoInfoACodec.class);

    @Override
    public Class<AlarmVideoInfoDto> getSupportedDtoClass() {
        return AlarmVideoInfoDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xFE79;
    }

    @Override
    public String getParameterName() {
        return "Alarm video information_A";
    }

    @Override
    protected AlarmVideoInfoDto mapToDto(byte[] decodedValueBytes) {
        if (decodedValueBytes == null || decodedValueBytes.length < 1) {
            return null;
        }

        AlarmVideoInfoDto dto = new AlarmVideoInfoDto();
        dto.setParameterId(getSupportedParameterId());
        dto.setGroup('A');

        ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes);

        try {
            // 1. Ekstrak Version
            dto.setVersion(buffer.get() & 0xFF);

            // 2. Looping selama masih ada minimal 3 byte: CH_Num (1) + CH_Type (1) + NameLen (1)
            while (buffer.remaining() >= 3) {
                AlarmVideoInfoDto.VideoItem item = new AlarmVideoInfoDto.VideoItem();

                item.setChannelNumber(buffer.get() & 0xFF);
                item.setChannelType(buffer.get() & 0xFF);

                int nameLen = buffer.get() & 0xFF;

                // Sabuk pengaman: Pastikan sisa array tidak kurang dari panjang nama video yang dijanjikan
                if (buffer.remaining() < nameLen) {
                    LOG.warnf("Data nama video terpotong untuk Parameter ID 0xFE79. Berhenti melakukan parsing sisa paket.");
                    break;
                }

                // 3. Ekstrak string nama video
                if (nameLen > 0) {
                    byte[] nameBytes = new byte[nameLen];
                    buffer.get(nameBytes);
                    item.setVideoName(new String(nameBytes, StandardCharsets.US_ASCII));
                } else {
                    item.setVideoName("");
                }

                dto.getVideoItems().add(item);
            }

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai AlarmVideoInfoA", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(AlarmVideoInfoDto dto) {
        // Alokasikan memori sementara yang cukup besar (bisa menampung beberapa nama video)
        ByteBuffer buffer = ByteBuffer.allocate(2048);

        buffer.put((byte) dto.getVersion());

        if (dto.getVideoItems() != null) {
            for (AlarmVideoInfoDto.VideoItem item : dto.getVideoItems()) {

                buffer.put((byte) item.getChannelNumber());
                buffer.put((byte) item.getChannelType());

                String name = item.getVideoName() != null ? item.getVideoName() : "";
                byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);

                // Pembatasan ekstrem: Panjang nama maksimal 125 byte sesuai dokumen
                int nameLen = Math.min(nameBytes.length, 125);

                buffer.put((byte) nameLen);
                buffer.put(nameBytes, 0, nameLen);
            }
        }

        // Potong buffer menjadi array final yang ukurannya persis dengan isinya
        byte[] result = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, result, 0, result.length);
        return result;
    }
}