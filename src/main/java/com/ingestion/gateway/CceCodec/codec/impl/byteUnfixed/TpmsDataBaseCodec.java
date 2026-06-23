// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.TpmsDataBaseCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.TpmsDataDto;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class TpmsDataBaseCodec extends AbstractByteUnfixedCodec<TpmsDataDto> {

    private static final Logger LOG = Logger.getLogger(TpmsDataBaseCodec.class);

    @Override
    public Class<TpmsDataDto> getSupportedDtoClass() {
        return TpmsDataDto.class;
    }

    @Override
    protected TpmsDataDto mapToDto(byte[] decodedValueBytes) {
        if (decodedValueBytes == null || decodedValueBytes.length < 1) {
            return null;
        }

        TpmsDataDto dto = new TpmsDataDto();
        // Set group secara otomatis berdasarkan ID yang sedang memprosesnya
        dto.setGroup(getSupportedParameterId() == 0xFEF2 ? 1 : 2);

        ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes).order(ByteOrder.LITTLE_ENDIAN);

        try {
            int numberOfTires = buffer.get() & 0xFF;

            // Setiap ban membutuhkan 8 byte. Validasi sisa buffer untuk mencegah error.
            while (buffer.remaining() >= 8 && dto.getItems().size() < numberOfTires) {
                TpmsDataDto.TpmsItem item = new TpmsDataDto.TpmsItem();

                // 1. Ekstrak Posisi Ban (1 byte)
                int numByte = buffer.get() & 0xFF;
                item.setVehiclePart((numByte >> 5) & 0x07); // Ambil 3 bit teratas
                item.setTireNumber(numByte & 0x1F);         // Ambil 5 bit terbawah

                // 2. Ekstrak ID Sensor (3 byte, Little-Endian)
                byte[] idBytes = new byte[3];
                buffer.get(idBytes);
                // Konversi ke Hex String (dibalik karena Little-Endian)
                item.setSensorId(String.format("%02X%02X%02X", idBytes[2], idBytes[1], idBytes[0]));

                // 3. Ekstrak Tekanan Ban (2 byte, Little-Endian)
                int rawPressure = buffer.getShort() & 0xFFFF;
                item.setTirePressureBar(rawPressure * 0.025);

                // 4. Ekstrak Suhu (1 byte)
                int rawTemp = buffer.get() & 0xFF;
                item.setTemperatureC(rawTemp - 50);

                // 5. Ekstrak Status (1 byte)
                int statusByte = buffer.get() & 0xFF;
                item.setTransmitterBatteryLow((statusByte & 0x80) != 0); // Bit 7
                item.setNoDataReceived((statusByte & 0x40) != 0);        // Bit 6
                // Bit 5 Reserved
                item.setHighAirPressure((statusByte & 0x10) != 0);       // Bit 4
                item.setLowAirPressure((statusByte & 0x08) != 0);        // Bit 3
                item.setHighTemperature((statusByte & 0x04) != 0);       // Bit 2
                item.setAlertStatus(statusByte & 0x03);                  // Bit 1-0

                dto.getItems().add(item);
            }

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai TPMS Data", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(TpmsDataDto dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return new byte[]{0}; // Jika tidak ada ban, kembalikan 0
        }

        // Batasi maksimal 16 ban sesuai dokumen spesifikasi
        int numberOfTires = Math.min(dto.getItems().size(), 16);

        // 1 byte Number + (8 byte * jumlah ban)
        ByteBuffer buffer = ByteBuffer.allocate(1 + (numberOfTires * 8)).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) numberOfTires);

        for (int i = 0; i < numberOfTires; i++) {
            TpmsDataDto.TpmsItem item = dto.getItems().get(i);

            // 1. Rakit Posisi Ban
            int part = (item.getVehiclePart() & 0x07) << 5;
            int tire = item.getTireNumber() & 0x1F;
            buffer.put((byte) (part | tire));

            // 2. Rakit ID Sensor (3 byte, dikembalikan ke Little-Endian)
            String sensorId = item.getSensorId() != null ? item.getSensorId() : "000000";
            // Pastikan panjang string pas 6 karakter hex
            sensorId = String.format("%-6s", sensorId).replace(' ', '0').substring(0, 6);
            buffer.put((byte) Integer.parseInt(sensorId.substring(4, 6), 16)); // ID[0]
            buffer.put((byte) Integer.parseInt(sensorId.substring(2, 4), 16)); // ID[1]
            buffer.put((byte) Integer.parseInt(sensorId.substring(0, 2), 16)); // ID[2]

            // 3. Rakit Tekanan Ban
            int rawPressure = (int) Math.round((item.getTirePressureBar() != null ? item.getTirePressureBar() : 0.0) / 0.025);
            buffer.putShort((short) rawPressure);

            // 4. Rakit Suhu
            int rawTemp = (item.getTemperatureC() != null ? item.getTemperatureC() : 0) + 50;
            buffer.put((byte) rawTemp);

            // 5. Rakit Status
            int statusByte = 0;
            if (item.isTransmitterBatteryLow()) statusByte |= 0x80;
            if (item.isNoDataReceived()) statusByte |= 0x40;
            if (item.isHighAirPressure()) statusByte |= 0x10;
            if (item.isLowAirPressure()) statusByte |= 0x08;
            if (item.isHighTemperature()) statusByte |= 0x04;
            statusByte |= (item.getAlertStatus() & 0x03);

            buffer.put((byte) statusByte);
        }

        return buffer.array();
    }
}