// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.TemperatureSensorInfoCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.TemperatureSensorInfoDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@ApplicationScoped
public class TemperatureSensorInfoCodec extends AbstractByteUnfixedCodec<TemperatureSensorInfoDto> {

    private static final Logger LOG = Logger.getLogger(TemperatureSensorInfoCodec.class);

    @Override
    public Class<TemperatureSensorInfoDto> getSupportedDtoClass() {
        return TemperatureSensorInfoDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xDB;
    }

    @Override
    public String getParameterName() {
        return "Temperature sensor information";
    }

    @Override
    protected TemperatureSensorInfoDto mapToDto(byte[] decodedValueBytes) {
        // Validasi minimal 2 byte: [Version] + [temp_num]
        if (decodedValueBytes == null || decodedValueBytes.length < 2) {
            return null;
        }

        TemperatureSensorInfoDto dto = new TemperatureSensorInfoDto();
        ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes).order(ByteOrder.LITTLE_ENDIAN);

        try {
            // 1. Ekstrak Version
            dto.setVersion(buffer.get() & 0xFF);

            if (dto.getVersion() == 1) {
                // 2. Ekstrak Jumlah Sensor
                int tempNum = buffer.get() & 0xFF;

                // 3. Looping untuk mengekstrak data per sensor (11 byte per sensor)
                while (buffer.remaining() >= 11 && dto.getSensors().size() < tempNum) {
                    TemperatureSensorInfoDto.TemperatureSensor sensor = new TemperatureSensorInfoDto.TemperatureSensor();

                    // Nomor Sensor (1 byte)
                    sensor.setSensorNumber(buffer.get() & 0xFF);

                    // Serial Number (8 byte)
                    byte[] snBytes = new byte[8];
                    buffer.get(snBytes);
                    StringBuilder snBuilder = new StringBuilder();
                    for (byte b : snBytes) {
                        snBuilder.append(String.format("%02X ", b)); // Tambahkan spasi agar persis seperti dokumen
                    }
                    sensor.setSerialNumber(snBuilder.toString().trim());

                    // Suhu (2 byte Signed, Little-Endian, dibagi 100)
                    short rawTemp = buffer.getShort();
                    sensor.setTemperatureCelcius(rawTemp / 100.0);

                    dto.getSensors().add(sensor);
                }
            } else {
                LOG.warnf("Versi sensor suhu tidak didukung: %d. Mengabaikan sisa data.", dto.getVersion());
            }

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai TemperatureSensorInfo", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(TemperatureSensorInfoDto dto) {
        int sensorCount = dto.getSensors() != null ? dto.getSensors().size() : 0;

        // Batasi sensorCount ke 255 (maksimal 1 byte)
        sensorCount = Math.min(sensorCount, 255);

        // Alokasi memori: 1 (Version) + 1 (Num) + (Sensor * 11 byte)
        ByteBuffer buffer = ByteBuffer.allocate(2 + (sensorCount * 11)).order(ByteOrder.LITTLE_ENDIAN);

        // 1. Tulis Version
        buffer.put((byte) dto.getVersion());

        // 2. Tulis Jumlah Sensor
        buffer.put((byte) sensorCount);

        // 3. Tulis Data Sensor
        if (sensorCount > 0) {
            for (int i = 0; i < sensorCount; i++) {
                TemperatureSensorInfoDto.TemperatureSensor sensor = dto.getSensors().get(i);

                // Tulis Nomor Sensor
                buffer.put((byte) sensor.getSensorNumber());

                // Tulis Serial Number
                String sn = sensor.getSerialNumber() != null ? sensor.getSerialNumber().replace(" ", "") : "0000000000000000";
                sn = String.format("%-16s", sn).replace(' ', '0').substring(0, 16); // Pastikan persis 16 karakter Hex (8 byte)

                for (int j = 0; j < 8; j++) {
                    String hexByte = sn.substring(j * 2, j * 2 + 2);
                    buffer.put((byte) Integer.parseInt(hexByte, 16));
                }

                // Tulis Suhu (Dikali 100)
                short rawTemp = (short) Math.round(sensor.getTemperatureCelcius() * 100.0);
                buffer.putShort(rawTemp);
            }
        }

        return buffer.array();
    }
}