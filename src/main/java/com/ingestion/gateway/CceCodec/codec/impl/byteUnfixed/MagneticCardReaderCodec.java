// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.MagneticCardReaderCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.MagneticCardReaderDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class MagneticCardReaderCodec extends AbstractByteUnfixedCodec<MagneticCardReaderDto> {

    private static final Logger LOG = Logger.getLogger(MagneticCardReaderCodec.class);

    @Override
    public Class<MagneticCardReaderDto> getSupportedDtoClass() {
        return MagneticCardReaderDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x39;
    }

    @Override
    public String getParameterName() {
        return "Magnetic Card Reader Information";
    }

    @Override
    protected MagneticCardReaderDto mapToDto(byte[] decodedValueBytes) {
        if (decodedValueBytes == null || decodedValueBytes.length < 1) {
            return null;
        }

        MagneticCardReaderDto dto = new MagneticCardReaderDto();

        try {
            // 1. Ekstrak RfidLen (1 byte di awal)
            int rfidLen = decodedValueBytes[0] & 0xFF;

            // 2. Validasi sisa buffer
            if (decodedValueBytes.length < 1 + rfidLen) {
                LOG.warn("Data Rfid terpotong. Membaca seadanya dari sisa buffer.");
                rfidLen = decodedValueBytes.length - 1;
            }

            // 3. Ekstrak RfidData menjadi String ASCII
            if (rfidLen > 0) {
                String rfidData = new String(decodedValueBytes, 1, rfidLen, StandardCharsets.US_ASCII);
                dto.setRfidData(rfidData);
            } else {
                dto.setRfidData("");
            }

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai MagneticCardReader", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(MagneticCardReaderDto dto) {
        String data = dto.getRfidData() != null ? dto.getRfidData() : "";
        byte[] dataBytes = data.getBytes(StandardCharsets.US_ASCII);

        // Batasi panjang maksimal 160 byte sesuai spesifikasi dokumen
        int rfidLen = Math.min(dataBytes.length, 160);

        // Siapkan array hasil: 1 byte (RfidLen) + n byte (RfidData)
        byte[] result = new byte[1 + rfidLen];

        result[0] = (byte) (rfidLen & 0xFF);
        System.arraycopy(dataBytes, 0, result, 1, rfidLen);

        return result;
    }
}