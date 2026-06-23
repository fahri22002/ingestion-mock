// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.AspcPeopleCounterCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.AspcPeopleCounterDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@ApplicationScoped
public class AspcPeopleCounterCodec extends AbstractByteUnfixedCodec<AspcPeopleCounterDto> {

    private static final Logger LOG = Logger.getLogger(AspcPeopleCounterCodec.class);

    @Override
    public Class<AspcPeopleCounterDto> getSupportedDtoClass() {
        return AspcPeopleCounterDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xFE96;
    }

    @Override
    public String getParameterName() {
        return "ASPC People Counter";
    }

    @Override
    protected AspcPeopleCounterDto mapToDto(byte[] decodedValueBytes) {
        // Validasi panjang minimal (1 byte Version + 20 byte AllSensorData) = 21 byte
        if (decodedValueBytes == null || decodedValueBytes.length < 21) {
            return null;
        }

        AspcPeopleCounterDto dto = new AspcPeopleCounterDto();
        ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes).order(ByteOrder.LITTLE_ENDIAN);

        try {
            // 1. Ekstrak Version
            dto.setVersion(buffer.get() & 0xFF);

            // 2. Ekstrak Data Individual Sensor
            // Rumus Logika: Selama sisa buffer >= 39 (19 byte sensor + 20 byte rekap AllSensor), 
            // berarti kursor kita masih berada di zona data individual sensor.
            while (buffer.remaining() >= 39) {
                AspcPeopleCounterDto.SensorData sensor = new AspcPeopleCounterDto.SensorData();

                sensor.setNumber(buffer.get() & 0xFF);
                sensor.setDoorNumber(buffer.get() & 0xFF);
                sensor.setState(buffer.get() & 0xFF);

                // Ekstrak DWORD (4 byte Unsigned). Di Java, dibaca sebagai int lalu di-masking ke long
                sensor.setUpCar(buffer.getInt() & 0xFFFFFFFFL);
                sensor.setDownCar(buffer.getInt() & 0xFFFFFFFFL);
                sensor.setAllUpCar(buffer.getInt() & 0xFFFFFFFFL);
                sensor.setAllDownCar(buffer.getInt() & 0xFFFFFFFFL);

                dto.getSensors().add(sensor);
            }

            // 3. Ekstrak Rekapitulasi Data (All Sensor Data)
            if (buffer.remaining() >= 20) {
                AspcPeopleCounterDto.AllSensorData allData = new AspcPeopleCounterDto.AllSensorData();

                allData.setUpCar(buffer.getInt() & 0xFFFFFFFFL);
                allData.setDownCar(buffer.getInt() & 0xFFFFFFFFL);
                allData.setAllUpCar(buffer.getInt() & 0xFFFFFFFFL);
                allData.setAllDownCar(buffer.getInt() & 0xFFFFFFFFL);
                allData.setSurplus(buffer.getInt() & 0xFFFFFFFFL);

                dto.setAllSensorData(allData);
            } else {
                LOG.warn("Kehilangan blok rekapitulasi All Sensor Data karena array terpotong!");
            }

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai ASPC People Counter", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(AspcPeopleCounterDto dto) {
        int sensorCount = dto.getSensors() != null ? dto.getSensors().size() : 0;

        // Batasi maksimal 4 sensor sesuai aturan dokumen
        sensorCount = Math.min(sensorCount, 4);

        // Alokasi memori secara presisi: 1 (Version) + (Sensor * 19) + 20 (All Data)
        int totalLength = 1 + (sensorCount * 19) + 20;
        ByteBuffer buffer = ByteBuffer.allocate(totalLength).order(ByteOrder.LITTLE_ENDIAN);

        // 1. Tulis Version
        buffer.put((byte) dto.getVersion());

        // 2. Tulis Data Individual Sensor
        if (sensorCount > 0) {
            for (int i = 0; i < sensorCount; i++) {
                AspcPeopleCounterDto.SensorData sensor = dto.getSensors().get(i);

                buffer.put((byte) sensor.getNumber());
                buffer.put((byte) sensor.getDoorNumber());
                buffer.put((byte) sensor.getState());

                // Tulis tipe data long ke int (otomatis 4 byte)
                buffer.putInt((int) sensor.getUpCar());
                buffer.putInt((int) sensor.getDownCar());
                buffer.putInt((int) sensor.getAllUpCar());
                buffer.putInt((int) sensor.getAllDownCar());
            }
        }

        // 3. Tulis Rekapitulasi Data (All Sensor Data)
        AspcPeopleCounterDto.AllSensorData allData = dto.getAllSensorData();
        if (allData != null) {
            buffer.putInt((int) allData.getUpCar());
            buffer.putInt((int) allData.getDownCar());
            buffer.putInt((int) allData.getAllUpCar());
            buffer.putInt((int) allData.getAllDownCar());
            buffer.putInt((int) allData.getSurplus());
        } else {
            // Jika object null, isi sisa 20 byte dengan 0x00 sebagai pengaman
            buffer.put(new byte[20]);
        }

        return buffer.array();
    }
}