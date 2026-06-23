// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.OverspeedEventInfoCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.OverspeedEventInfoDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@ApplicationScoped
public class OverspeedEventInfoCodec extends AbstractByteUnfixedCodec<OverspeedEventInfoDto> {

    private static final Logger LOG = Logger.getLogger(OverspeedEventInfoCodec.class);

    @Override
    public Class<OverspeedEventInfoDto> getSupportedDtoClass() {
        return OverspeedEventInfoDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xF82E;
    }

    @Override
    public String getParameterName() {
        return "Overspeed Event Information Statistics";
    }

    @Override
    protected OverspeedEventInfoDto mapToDto(byte[] decodedValueBytes) {
        // Validasi panjang harus tepat 32 byte (0x20)
        if (decodedValueBytes == null || decodedValueBytes.length < 32) {
            return null;
        }

        OverspeedEventInfoDto dto = new OverspeedEventInfoDto();
        ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes).order(ByteOrder.LITTLE_ENDIAN);

        try {
            // 1. Ekstrak Waktu Mulai (Unsigned 4 byte)
            dto.setOverspeedStartTime(buffer.getInt() & 0xFFFFFFFFL);

            // 2. Ekstrak Koordinat Mulai (Dibagi 1.000.000 untuk mendapatkan derajat desimal asli)
            dto.setOverspeedStartLongitude(buffer.getInt() / 1_000_000.0);
            dto.setOverspeedStartLatitude(buffer.getInt() / 1_000_000.0);

            // 3. Ekstrak Waktu Selesai (Unsigned 4 byte)
            dto.setOverspeedEndTime(buffer.getInt() & 0xFFFFFFFFL);

            // 4. Ekstrak Koordinat Selesai
            dto.setOverspeedEndLongitude(buffer.getInt() / 1_000_000.0);
            dto.setOverspeedEndLatitude(buffer.getInt() / 1_000_000.0);

            // 5. Ekstrak Durasi Overspeed
            dto.setOverspeedDuration(buffer.getInt() & 0xFFFFFFFFL);

            // 6. Ekstrak Kecepatan Rata-rata dan Maksimal (Dibagi 10 untuk nilai KM/H asli)
            // Menggunakan Unsigned 2 byte (short)
            dto.setAverageSpeed((buffer.getShort() & 0xFFFF) / 10.0);
            dto.setMaximumSpeed((buffer.getShort() & 0xFFFF) / 10.0);

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai OverspeedEventInfo", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(OverspeedEventInfoDto dto) {
        // Alokasi memori secara presisi (32 byte mutlak)
        ByteBuffer buffer = ByteBuffer.allocate(32).order(ByteOrder.LITTLE_ENDIAN);

        // 1. Rakit Waktu Mulai
        buffer.putInt((int) dto.getOverspeedStartTime());

        // 2. Rakit Koordinat Mulai (Dikali 1.000.000 untuk kembali ke format sepersejuta derajat)
        buffer.putInt((int) Math.round(dto.getOverspeedStartLongitude() * 1_000_000.0));
        buffer.putInt((int) Math.round(dto.getOverspeedStartLatitude() * 1_000_000.0));

        // 3. Rakit Waktu Selesai
        buffer.putInt((int) dto.getOverspeedEndTime());

        // 4. Rakit Koordinat Selesai
        buffer.putInt((int) Math.round(dto.getOverspeedEndLongitude() * 1_000_000.0));
        buffer.putInt((int) Math.round(dto.getOverspeedEndLatitude() * 1_000_000.0));

        // 5. Rakit Durasi
        buffer.putInt((int) dto.getOverspeedDuration());

        // 6. Rakit Kecepatan (Dikali 10)
        buffer.putShort((short) Math.round(dto.getAverageSpeed() * 10.0));
        buffer.putShort((short) Math.round(dto.getMaximumSpeed() * 10.0));

        return buffer.array();
    }
}