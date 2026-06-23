// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.FacialRecognitionAlarmCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.FacialRecognitionAlarmDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class FacialRecognitionAlarmCodec extends AbstractByteUnfixedCodec<FacialRecognitionAlarmDto> {

    private static final Logger LOG = Logger.getLogger(FacialRecognitionAlarmCodec.class);

    @Override
    public Class<FacialRecognitionAlarmDto> getSupportedDtoClass() {
        return FacialRecognitionAlarmDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xFE6A;
    }

    @Override
    public String getParameterName() {
        return "Facial Recognition Alarm Assistance Information";
    }

    @Override
    protected FacialRecognitionAlarmDto mapToDto(byte[] decodedValueBytes) {
        // Validasi panjang minimal: Protocol (1) + Type (1) + PhotoName (64) = 66 byte
        if (decodedValueBytes == null || decodedValueBytes.length < 66) {
            return null;
        }

        FacialRecognitionAlarmDto dto = new FacialRecognitionAlarmDto();
        dto.setParameterId(getSupportedParameterId());

        try {
            ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes);

            // 1. Ekstrak Alarm Protocol
            dto.setAlarmProtocol(buffer.get() & 0xFF);

            // 2. Ekstrak Alarm Type
            dto.setAlarmType(buffer.get() & 0xFF);

            // 3. Ekstrak 64 byte data Photo Name
            byte[] photoBytes = new byte[64];
            buffer.get(photoBytes);

            // Konversi ke ASCII dan bersihkan karakter null (\0) yang tersisa
            String photoName = new String(photoBytes, StandardCharsets.US_ASCII);
            photoName = photoName.replace("\0", "").trim();

            dto.setPhotoName(photoName);

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai FacialRecognitionAlarm", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(FacialRecognitionAlarmDto dto) {
        // Alokasikan memori persis 66 byte (Sisa buffer otomatis terinisialisasi menjadi 0x00 di Java)
        ByteBuffer buffer = ByteBuffer.allocate(66);

        // 1. Tulis Protocol
        buffer.put((byte) dto.getAlarmProtocol());

        // 2. Tulis Type
        buffer.put((byte) dto.getAlarmType());

        // 3. Tulis Photo Name
        String name = dto.getPhotoName() != null ? dto.getPhotoName() : "";
        byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);

        // Batasi panjang nama maksimal 64 byte agar tidak buffer-overflow
        int copyLength = Math.min(nameBytes.length, 64);

        // Tulis bytes nama (hanya sebanyak copyLength). Sisa dari 64 byte slot akan tetap bernilai 0x00
        buffer.put(nameBytes, 0, copyLength);

        return buffer.array();
    }
}