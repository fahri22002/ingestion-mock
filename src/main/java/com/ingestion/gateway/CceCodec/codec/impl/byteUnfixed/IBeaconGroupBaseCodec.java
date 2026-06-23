// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.IBeaconGroupBaseCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.IBeaconGroupBaseDto;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Menggunakan Generic IBeaconGroupBaseDto yang merupakan turunan dari IBeaconGroupBaseDto
public abstract class IBeaconGroupBaseCodec extends AbstractByteUnfixedCodec<IBeaconGroupBaseDto> {

    private static final Logger LOG = Logger.getLogger(IBeaconGroupBaseCodec.class);

    @Override
    public Class<IBeaconGroupBaseDto> getSupportedDtoClass() {
        return IBeaconGroupBaseDto.class;
    }

    @Override
    protected IBeaconGroupBaseDto mapToDto(byte[] decodedValueBytes) {
        if (decodedValueBytes == null || decodedValueBytes.length < 1) {
            return null;
        }

        // Membuat instance DTO sesuai dengan pemanggil (Group A atau B)
        IBeaconGroupBaseDto dto = new IBeaconGroupBaseDto();
        ByteBuffer buffer = ByteBuffer.wrap(decodedValueBytes).order(ByteOrder.BIG_ENDIAN);

        try {
            dto.setVersion(buffer.get() & 0xFF);

            while (buffer.remaining() >= 9) {
                IBeaconGroupBaseDto.IBeaconItem beacon = new IBeaconGroupBaseDto.IBeaconItem();

                int nameLen = buffer.get() & 0xFF;
                if (buffer.remaining() < nameLen + 8) break;

                if (nameLen > 0) {
                    byte[] nameBytes = new byte[nameLen];
                    buffer.get(nameBytes);
                    beacon.setDeviceName(new String(nameBytes, StandardCharsets.US_ASCII));
                } else {
                    beacon.setDeviceName("");
                }

                byte[] macBytes = new byte[6];
                buffer.get(macBytes);
                StringBuilder macBuilder = new StringBuilder();
                for (byte b : macBytes) {
                    macBuilder.append(String.format("%02X", b));
                }
                beacon.setMacAddress(macBuilder.toString());

                beacon.setBatteryLevel(buffer.get() & 0xFF);
                beacon.setSignalStrength(buffer.get());

                dto.getBeacons().add(beacon);
            }
            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai IBeaconGroup", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(IBeaconGroupBaseDto dto) {
        ByteBuffer buffer = ByteBuffer.allocate(1024).order(ByteOrder.BIG_ENDIAN);
        buffer.put((byte) dto.getVersion());

        List<IBeaconGroupBaseDto.IBeaconItem> beacons = dto.getBeacons();
        if (beacons != null) {
            for (IBeaconGroupBaseDto.IBeaconItem beacon : beacons) {
                String name = beacon.getDeviceName() != null ? beacon.getDeviceName() : "";
                byte[] nameBytes = name.getBytes(StandardCharsets.US_ASCII);
                int nameLen = Math.min(nameBytes.length, 16);
                buffer.put((byte) nameLen);
                buffer.put(nameBytes, 0, nameLen);

                String mac = beacon.getMacAddress() != null ? beacon.getMacAddress() : "000000000000";
                mac = String.format("%-12s", mac).replace(' ', '0').substring(0, 12);
                for (int i = 0; i < 6; i++) {
                    String hexByte = mac.substring(i * 2, i * 2 + 2);
                    buffer.put((byte) Integer.parseInt(hexByte, 16));
                }

                buffer.put((byte) beacon.getBatteryLevel());
                buffer.put(beacon.getSignalStrength());
            }
        }

        byte[] result = new byte[buffer.position()];
        System.arraycopy(buffer.array(), 0, result, 0, result.length);
        return result;
    }
}