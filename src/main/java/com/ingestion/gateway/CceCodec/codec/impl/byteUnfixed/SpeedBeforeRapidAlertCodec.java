// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.SpeedBeforeRapidAlertCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.SpeedBeforeRapidAlertDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class SpeedBeforeRapidAlertCodec extends AbstractByteUnfixedCodec<SpeedBeforeRapidAlertDto> {

    private static final Logger LOG = Logger.getLogger(SpeedBeforeRapidAlertCodec.class);

    @Override
    public Class<SpeedBeforeRapidAlertDto> getSupportedDtoClass() {
        return SpeedBeforeRapidAlertDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xF824;
    }

    @Override
    public String getParameterName() {
        return "Speed data before rapid acceleration/deceleration alerts";
    }

    @Override
    protected SpeedBeforeRapidAlertDto mapToDto(byte[] decodedValueBytes) {
        // Validasi minimal 3 byte: [Number (1)] + [Unit Interval (2)]
        if (decodedValueBytes == null || decodedValueBytes.length < 3) {
            return null;
        }

        SpeedBeforeRapidAlertDto dto = new SpeedBeforeRapidAlertDto();
        try {
            // 1. Baca jumlah data kecepatan (Number)
            int number = decodedValueBytes[0] & 0xFF;

            // 2. Baca Unit Interval (2 byte, Little-Endian)
            int unitIntervalMs = (decodedValueBytes[1] & 0xFF) | ((decodedValueBytes[2] & 0xFF) << 8);
            dto.setUnitIntervalMs(unitIntervalMs);

            // 3. Validasi apakah sisa byte cukup untuk menampung 'number' kecepatan
            int expectedLength = 3 + number;
            if (decodedValueBytes.length < expectedLength) {
                LOG.warnf("Panjang data 0xF824 kurang. Diharapkan: %d, Aktual: %d. Membaca seadanya.", expectedLength, decodedValueBytes.length);
                number = decodedValueBytes.length - 3; // Koreksi jumlah agar tidak IndexOutOfBounds
            }

            // 4. Looping untuk mengambil nilai kecepatan (1 byte per nilai)
            for (int i = 0; i < number; i++) {
                int speed = decodedValueBytes[3 + i] & 0xFF;
                dto.getSpeedValues().add(speed);
            }

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai SpeedBeforeRapidAlert", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(SpeedBeforeRapidAlertDto dto) {
        List<Integer> speeds = dto.getSpeedValues();
        int number = (speeds != null) ? speeds.size() : 0;

        // Batasi jumlah nilai maksimal 255 (karena tipe datanya 1 byte)
        number = Math.min(number, 255);

        // Alokasi memori: 1 byte (Number) + 2 byte (Interval) + n byte (Kecepatan)
        byte[] result = new byte[3 + number];

        // 1. Tulis Number
        result[0] = (byte) (number & 0xFF);

        // 2. Tulis Interval (Little-Endian)
        int interval = dto.getUnitIntervalMs();
        result[1] = (byte) (interval & 0xFF);
        result[2] = (byte) ((interval >> 8) & 0xFF);

        // 3. Tulis semua nilai kecepatan
        for (int i = 0; i < number; i++) {
            result[3 + i] = (byte) (speeds.get(i) & 0xFF);
        }

        return result;
    }
}