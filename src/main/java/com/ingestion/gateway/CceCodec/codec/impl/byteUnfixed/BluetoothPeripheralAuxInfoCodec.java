// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.BluetoothPeripheralAuxInfoCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.BluetoothPeripheralAuxInfoDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class BluetoothPeripheralAuxInfoCodec extends AbstractByteUnfixedCodec<BluetoothPeripheralAuxInfoDto> {

    private static final Logger LOG = Logger.getLogger(BluetoothPeripheralAuxInfoCodec.class);

    @Override
    public Class<BluetoothPeripheralAuxInfoDto> getSupportedDtoClass() {
        return BluetoothPeripheralAuxInfoDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xFE70;
    }

    @Override
    public String getParameterName() {
        return "Bluetooth peripheral auxiliary information";
    }

    @Override
    protected BluetoothPeripheralAuxInfoDto mapToDto(byte[] decodedValueBytes) {
        if (decodedValueBytes == null || decodedValueBytes.length < 2) {
            return null;
        }

        BluetoothPeripheralAuxInfoDto dto = new BluetoothPeripheralAuxInfoDto();
        // PERHATIAN: Parameter ini menggunakan BIG_ENDIAN sesuai spesifikasi!
        ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes).order(ByteOrder.BIG_ENDIAN);

        try {
            dto.setVersion(buffer.get() & 0xFF);
            int type = buffer.get() & 0xFF;
            dto.setAlarmType(type);

            if (type == 11 || type == 12) {
                if (buffer.remaining() >= 4) {
                    dto.setMajor(buffer.getShort() & 0xFFFF);
                    dto.setMinor(buffer.getShort() & 0xFFFF);
                }
            } else {
                if (buffer.remaining() < 1) return dto;

                // 1. Ekstrak Nama Device
                int nameLen = buffer.get() & 0xFF;
                if (buffer.remaining() >= nameLen) {
                    byte[] nameBytes = new byte[nameLen];
                    buffer.get(nameBytes);
                    dto.setDeviceName(new String(nameBytes, StandardCharsets.US_ASCII));
                }

                // 2. Ekstrak MAC Address (6 byte)
                if (buffer.remaining() >= 6) {
                    byte[] macBytes = new byte[6];
                    buffer.get(macBytes);
                    StringBuilder macBuilder = new StringBuilder();
                    for (byte b : macBytes) {
                        macBuilder.append(String.format("%02X", b));
                    }
                    dto.setMacAddress(macBuilder.toString());
                }

                // 3. Cabang logika berdasarkan tipe alarm
                if (type >= 1 && type <= 5) {
                    if (buffer.remaining() >= 5) {
                        dto.setBatteryLevel(buffer.get() & 0xFF);

                        // Konversi Signed 8.8 Fixed-Point
                        short rawTemp = buffer.getShort();
                        dto.setTemperature(rawTemp / 256.0);

                        short rawHum = buffer.getShort();
                        dto.setHumidity(rawHum / 256.0);
                    }
                } else if (type == 8) {
                    if (buffer.remaining() >= 2) {
                        // Antisipasi anomali byte "02" dari contoh dokumen hex
                        if (buffer.remaining() == 3) {
                            buffer.get(); // Skip 1 byte ekstra jika ditemukan
                        }
                        dto.setBatteryLevel(buffer.get() & 0xFF);
                        dto.setSignalStrength(buffer.get()); // Signed byte
                    }
                }
            }
            return dto;

        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai BluetoothPeripheralAuxInfo", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(BluetoothPeripheralAuxInfoDto dto) {
        // Alokasi memori berlebih untuk keamanan, nanti dipotong sesuai kapasitas asli
        ByteBuffer buffer = ByteBuffer.allocate(64).order(ByteOrder.BIG_ENDIAN);

        buffer.put((byte) dto.getVersion());
        buffer.put((byte) dto.getAlarmType());

        int type = dto.getAlarmType();

        if (type == 11 || type == 12) {
            buffer.putShort((short) (dto.getMajor() != null ? dto.getMajor() : 0));
            buffer.putShort((short) (dto.getMinor() != null ? dto.getMinor() : 0));
        } else {
            // Tulis Nama
            String name = dto.getDeviceName() != null ? dto.getDeviceName() : "";
            byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
            int nameLen = Math.min(nameBytes.length, 16); // Maksimal 16 byte
            buffer.put((byte) nameLen);
            buffer.put(nameBytes, 0, nameLen);

            // Tulis MAC Address
            String mac = dto.getMacAddress() != null ? dto.getMacAddress() : "000000000000";
            mac = String.format("%-12s", mac).replace(' ', '0').substring(0, 12);
            for (int i = 0; i < 6; i++) {
                String hexByte = mac.substring(i * 2, i * 2 + 2);
                buffer.put((byte) Integer.parseInt(hexByte, 16));
            }

            if (type >= 1 && type <= 5) {
                buffer.put((byte) (dto.getBatteryLevel() != null ? dto.getBatteryLevel() : 0));

                // Kembalikan ke format 8.8 Fixed-Point
                short rawTemp = (short) Math.round((dto.getTemperature() != null ? dto.getTemperature() : 0.0) * 256.0);
                buffer.putShort(rawTemp);

                short rawHum = (short) Math.round((dto.getHumidity() != null ? dto.getHumidity() : 0.0) * 256.0);
                buffer.putShort(rawHum);

            } else if (type == 8) {
                buffer.put((byte) (dto.getBatteryLevel() != null ? dto.getBatteryLevel() : 0));
                buffer.put((byte) (dto.getSignalStrength() != null ? dto.getSignalStrength() : 0));
            }
        }

        // Potong array hanya sebatas yang terisi
        byte[] result = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, result, 0, result.length);
        return result;
    }
}