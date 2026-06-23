package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.TrackerRegistrationDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@ApplicationScoped
public class TrackerRegistrationCodec implements Jtt808MessageCodec<TrackerRegistrationDto> {

    private static final Logger LOG = Logger.getLogger(TrackerRegistrationCodec.class);

    // Protokol menetapkan tipe STRING menggunakan encoding GBK
    private static final Charset GBK = Charset.forName("GBK");

    @Override
    public int getSupportedMessageId() {
        return 0x0100; // ID untuk Tracker Registration
    }

    @Override
    public Class<TrackerRegistrationDto> getSupportedDtoClass() {
        return TrackerRegistrationDto.class;
    }

    @Override
    public String getCommandName() {
        return "Tracker Registration";
    }

    @Override
    public TrackerRegistrationDto decodeBody(byte[] bodyData) {
        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyData);
        TrackerRegistrationDto dto = new TrackerRegistrationDto();

        try {
            // WORD (2 byte): Province ID [cite: 98]
            int provinceId = Short.toUnsignedInt(bodyBuffer.getShort());

            // WORD (2 byte): City and County ID [cite: 98, 99]
            int cityId = Short.toUnsignedInt(bodyBuffer.getShort());

            // BYTE[5]: Manufacturer ID
            byte[] manufacturerIdBytes = new byte[5];
            bodyBuffer.get(manufacturerIdBytes);
            String manufacturerId = new String(manufacturerIdBytes, GBK).trim();

            // BYTE[20]: Terminal Model
            byte[] terminalModelBytes = new byte[20];
            bodyBuffer.get(terminalModelBytes);
            String terminalModel = new String(terminalModelBytes, GBK).trim();

            // BYTE[7]: Terminal Model ID
            byte[] terminalIdBytes = new byte[7];
            bodyBuffer.get(terminalIdBytes);
            String terminalId = new String(terminalIdBytes, GBK).trim();

            // BYTE (1 byte): License Plate Color
            int plateColor = Byte.toUnsignedInt(bodyBuffer.get());

            // STRING (Sisa buffer): Vehicle Identification
            byte[] vehicleIdBytes = new byte[bodyBuffer.remaining()];
            bodyBuffer.get(vehicleIdBytes);
            String vehicleId = new String(vehicleIdBytes, GBK).trim();

            dto.setProvinceId(provinceId);
            dto.setCityAndCountryId(cityId);
            dto.setManufacturerId(manufacturerId);
            dto.setTerminalModel(terminalModel);
            dto.setTerminalModelId(terminalId);
            dto.setLicensePlateColor(plateColor);
            dto.setVehicleIdentification(vehicleId);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Province ID     : %d", provinceId);
            LOG.infof("City/County ID  : %d", cityId);
            LOG.infof("Manufacturer ID : %s", manufacturerId);
            LOG.infof("Terminal Model  : %s", terminalModel);
            LOG.infof("Terminal ID     : %s", terminalId);
            LOG.infof("Plate Color     : %d", plateColor);
            LOG.infof("Vehicle ID      : %s", vehicleId);

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Tracker Registration", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(TrackerRegistrationDto dto) {
        try {
            // Encode Vehicle ID terlebih dahulu untuk mengetahui panjang byte sisanya
            byte[] vehicleIdBytes = new byte[0];
            if (dto.getVehicleIdentification() != null) {
                vehicleIdBytes = dto.getVehicleIdentification().getBytes(GBK);
            }

            // Total panjang data konstan = 2 + 2 + 5 + 20 + 7 + 1 = 37 byte
            // Ditambah panjang dinamis dari Vehicle ID
            ByteBuffer buffer = ByteBuffer.allocate(37 + vehicleIdBytes.length);

            // WORD (2 byte)
            buffer.putShort((short) dto.getProvinceId());

            // WORD (2 byte)
            buffer.putShort((short) dto.getCityAndCountryId());

            // BYTE[5]: Manufacturer ID
            buffer.put(getFixedLengthBytes(dto.getManufacturerId(), 5));

            // BYTE[20]: Terminal Model
            buffer.put(getFixedLengthBytes(dto.getTerminalModel(), 20));

            // BYTE[7]: Terminal Model ID
            buffer.put(getFixedLengthBytes(dto.getTerminalModelId(), 7));

            // BYTE (1 byte)
            buffer.put((byte) dto.getLicensePlateColor());

            // STRING (Sisa buffer)
            buffer.put(vehicleIdBytes);

            return buffer.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Tracker Registration", e);
            return new byte[0];
        }
    }

    /**
     * Helper method untuk memastikan array byte memiliki panjang yang tetap.
     * Jika string asal lebih pendek, sisa array akan tetap 0x00 (zero-padded).
     * Jika string asal lebih panjang, string akan dipotong.
     */
    private byte[] getFixedLengthBytes(String value, int requiredLength) {
        byte[] fixedBytes = new byte[requiredLength];
        if (value != null) {
            byte[] rawBytes = value.getBytes(GBK);
            System.arraycopy(rawBytes, 0, fixedBytes, 0, Math.min(rawBytes.length, requiredLength));
        }
        return fixedBytes;
    }
}