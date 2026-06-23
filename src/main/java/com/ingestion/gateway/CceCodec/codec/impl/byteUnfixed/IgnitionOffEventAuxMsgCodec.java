// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.IgnitionOffEventAuxMsgCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.IgnitionOffEventAuxMsgDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@ApplicationScoped
public class IgnitionOffEventAuxMsgCodec extends AbstractByteUnfixedCodec<IgnitionOffEventAuxMsgDto> {

    private static final Logger LOG = Logger.getLogger(IgnitionOffEventAuxMsgCodec.class);

    @Override
    public Class<IgnitionOffEventAuxMsgDto> getSupportedDtoClass() {
        return IgnitionOffEventAuxMsgDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x6A;
    }

    @Override
    public String getParameterName() {
        return "Ignition Off Event Auxiliary Message";
    }

    @Override
    protected IgnitionOffEventAuxMsgDto mapToDto(byte[] decodedValueBytes) {
        // Validasi panjang harus tepat 15 byte
        if (decodedValueBytes == null || decodedValueBytes.length < 15) {
            return null;
        }

        IgnitionOffEventAuxMsgDto dto = new IgnitionOffEventAuxMsgDto();
        ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes).order(ByteOrder.LITTLE_ENDIAN);

        try {
            // 1. Ekstrak Version (1 byte)
            dto.setVersion(buffer.get() & 0xFF);

            // 2. Ekstrak Trip Distance (Unsigned 4 byte)
            dto.setTripDistanceMeters(buffer.getInt() & 0xFFFFFFFFL);

            // 3. Ekstrak Trip Duration (Unsigned 4 byte)
            dto.setTripDurationSeconds(buffer.getInt() & 0xFFFFFFFFL);

            // 4. Ekstrak Kecepatan Rata-rata & Maksimal (Unsigned 2 byte)
            dto.setAverageSpeedKmh(buffer.getShort() & 0xFFFF);
            dto.setMaxSpeedKmh(buffer.getShort() & 0xFFFF);

            // 5. Ekstrak Konsumsi Bahan Bakar (Unsigned 2 byte, dikali 0.01)
            int rawFuel = buffer.getShort() & 0xFFFF;
            dto.setTripFuelConsumptionPercent(rawFuel * 0.01);

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai IgnitionOffEventAuxMsg", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(IgnitionOffEventAuxMsgDto dto) {
        // Alokasi memori secara presisi (15 byte mutlak)
        ByteBuffer buffer = ByteBuffer.allocate(15).order(ByteOrder.LITTLE_ENDIAN);

        // 1. Rakit Version
        buffer.put((byte) dto.getVersion());

        // 2. Rakit Trip Distance & Duration
        buffer.putInt((int) dto.getTripDistanceMeters());
        buffer.putInt((int) dto.getTripDurationSeconds());

        // 3. Rakit Kecepatan
        buffer.putShort((short) dto.getAverageSpeedKmh());
        buffer.putShort((short) dto.getMaxSpeedKmh());

        // 4. Rakit Konsumsi Bahan Bakar (Dibagi 0.01 untuk kembali ke format mentah)
        int rawFuel = (int) Math.round(dto.getTripFuelConsumptionPercent() / 0.01);
        buffer.putShort((short) rawFuel);

        return buffer.array();
    }
}