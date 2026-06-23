// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.BluetoothTempHumiditySensorCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.BluetoothTempHumiditySensorDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@ApplicationScoped
public class BluetoothTempHumiditySensorCodec extends AbstractByteUnfixedCodec<BluetoothTempHumiditySensorDto> {

    private static final Logger LOG = Logger.getLogger(BluetoothTempHumiditySensorCodec.class);

    @Override
    public Class<BluetoothTempHumiditySensorDto> getSupportedDtoClass() {
        return BluetoothTempHumiditySensorDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xFE73;
    }

    @Override
    public String getParameterName() {
        return "Bluetooth temperature and humidity sensor";
    }

    @Override
    protected BluetoothTempHumiditySensorDto mapToDto(byte[] decodedValueBytes) {
        if (decodedValueBytes == null || decodedValueBytes.length < 1) {
            return null;
        }

        BluetoothTempHumiditySensorDto dto = new BluetoothTempHumiditySensorDto();
        // PERHATIAN: Parameter spesifik ini menggunakan BIG_ENDIAN!
        ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes).order(ByteOrder.BIG_ENDIAN);

        try {
            dto.setVersion(buffer.get() & 0xFF);

            // Looping untuk mengantisipasi jika ada lebih dari 1 sensor dalam satu parameter
            // Minimal sisa buffer: 1 (NameLen) + 6 (MAC) + 1 (Batt) + 2 (Temp) + 2 (Hum) 
            // + 4 (TempAlarm) + 4 (HumAlarm) = 20 byte
            while (buffer.remaining() >= 20) {
                BluetoothTempHumiditySensorDto.SensorItem sensor = new BluetoothTempHumiditySensorDto.SensorItem();

                // 1. Ekstrak Nama Device
                int nameLen = buffer.get() & 0xFF;
                if (buffer.remaining() < nameLen + 19) break; // Validasi panjang sisa data

                if (nameLen > 0) {
                    byte[] nameBytes = new byte[nameLen];
                    buffer.get(nameBytes);
                    sensor.setDeviceName(new String(nameBytes, StandardCharsets.US_ASCII));
                } else {
                    sensor.setDeviceName("");
                }

                // 2. Ekstrak MAC Address (6 Byte)
                byte[] macBytes = new byte[6];
                buffer.get(macBytes);
                StringBuilder macBuilder = new StringBuilder();
                for (byte b : macBytes) {
                    macBuilder.append(String.format("%02X", b));
                }
                sensor.setMacAddress(macBuilder.toString());

                // 3. Ekstrak Baterai (1 Byte)
                sensor.setBatteryLevel(buffer.get() & 0xFF);

                // 4. Ekstrak Suhu & Kelembapan Utama (Format 8.8 Fixed-Point)
                sensor.setTemperature(buffer.getShort() / 256.0);
                sensor.setHumidity(buffer.getShort() / 256.0);

                // 5. Ekstrak Threshold Alarm Suhu (2 byte High, 2 byte Low)
                sensor.setHighTempAlarm(buffer.getShort() / 256.0);
                sensor.setLowTempAlarm(buffer.getShort() / 256.0);

                // 6. Ekstrak Threshold Alarm Kelembapan (2 byte High, 2 byte Low)
                sensor.setHighHumAlarm(buffer.getShort() / 256.0);
                sensor.setLowHumAlarm(buffer.getShort() / 256.0);

                dto.getSensors().add(sensor);
            }
            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai BluetoothTempHumiditySensor", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(BluetoothTempHumiditySensorDto dto) {
        ByteBuffer buffer = ByteBuffer.allocate(1024).order(ByteOrder.BIG_ENDIAN);

        buffer.put((byte) dto.getVersion());

        List<BluetoothTempHumiditySensorDto.SensorItem> sensors = dto.getSensors();
        if (sensors != null) {
            for (BluetoothTempHumiditySensorDto.SensorItem sensor : sensors) {

                // Tulis Name Length & Name
                String name = sensor.getDeviceName() != null ? sensor.getDeviceName() : "";
                byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
                int nameLen = Math.min(nameBytes.length, 16); // Maksimal 16 byte dari dokumen
                buffer.put((byte) nameLen);
                buffer.put(nameBytes, 0, nameLen);

                // Tulis MAC Address (6 Byte)
                String mac = sensor.getMacAddress() != null ? sensor.getMacAddress() : "000000000000";
                mac = String.format("%-12s", mac).replace(' ', '0').substring(0, 12);
                for (int i = 0; i < 6; i++) {
                    String hexByte = mac.substring(i * 2, i * 2 + 2);
                    buffer.put((byte) Integer.parseInt(hexByte, 16));
                }

                // Tulis Baterai
                buffer.put((byte) (sensor.getBatteryLevel() != null ? sensor.getBatteryLevel() : 0));

                // Tulis Suhu & Kelembapan Utama (Format 8.8 Fixed-Point)
                buffer.putShort((short) Math.round((sensor.getTemperature() != null ? sensor.getTemperature() : 0.0) * 256.0));
                buffer.putShort((short) Math.round((sensor.getHumidity() != null ? sensor.getHumidity() : 0.0) * 256.0));

                // Tulis Alarm Suhu
                buffer.putShort((short) Math.round((sensor.getHighTempAlarm() != null ? sensor.getHighTempAlarm() : 0.0) * 256.0));
                buffer.putShort((short) Math.round((sensor.getLowTempAlarm() != null ? sensor.getLowTempAlarm() : 0.0) * 256.0));

                // Tulis Alarm Kelembapan
                buffer.putShort((short) Math.round((sensor.getHighHumAlarm() != null ? sensor.getHighHumAlarm() : 0.0) * 256.0));
                buffer.putShort((short) Math.round((sensor.getLowHumAlarm() != null ? sensor.getLowHumAlarm() : 0.0) * 256.0));
            }
        }

        // Potong buffer menjadi array final yang presisi
        byte[] result = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, result, 0, result.length);
        return result;
    }
}