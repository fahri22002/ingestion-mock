// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.CurrentlyUsingNetworkInfoCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.CurrentlyUsingNetworkInfoDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class CurrentlyUsingNetworkInfoCodec extends AbstractByteUnfixedCodec<CurrentlyUsingNetworkInfoDto> {

    private static final Logger LOG = Logger.getLogger(CurrentlyUsingNetworkInfoCodec.class);

    @Override
    public Class<CurrentlyUsingNetworkInfoDto> getSupportedDtoClass() {
        return CurrentlyUsingNetworkInfoDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x4B;
    }

    @Override
    public String getParameterName() {
        return "Currently using network information";
    }

    @Override
    protected CurrentlyUsingNetworkInfoDto mapToDto(byte[] decodedValueBytes) {
        // Validasi minimal 3 byte: [Version (1)] + [Type (1)] + [DescriptorLen (1)]
        if (decodedValueBytes == null || decodedValueBytes.length < 3) {
            return null;
        }

        CurrentlyUsingNetworkInfoDto dto = new CurrentlyUsingNetworkInfoDto();
        try {
            // 1. Version (Index 0)
            dto.setVersion(decodedValueBytes[0] & 0xFF);

            // 2. Network Type (Index 1)
            dto.setNetworkType(decodedValueBytes[1] & 0xFF);

            // 3. Descriptor Length (Index 2)
            int descriptorLen = decodedValueBytes[2] & 0xFF;

            // 4. Descriptor String (Index 3 sampai selesai)
            if (descriptorLen > 0 && decodedValueBytes.length >= 3 + descriptorLen) {
                String descriptor = new String(decodedValueBytes, 3, descriptorLen, StandardCharsets.US_ASCII);
                dto.setDescriptor(descriptor);
            } else {
                dto.setDescriptor(""); // Pastikan tidak null jika tidak ada nama jaringan
            }

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai CurrentlyUsingNetworkInfo", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(CurrentlyUsingNetworkInfoDto dto) {
        // 1. Ambil byte dari string descriptor
        byte[] descriptorBytes = new byte[0];
        if (dto.getDescriptor() != null && !dto.getDescriptor().isEmpty()) {
            descriptorBytes = dto.getDescriptor().getBytes(StandardCharsets.US_ASCII);
        }

        // 2. Batasi panjang descriptor maksimum 32 byte sesuai spesifikasi
        int descriptorLen = Math.min(descriptorBytes.length, 32);

        // 3. Siapkan array hasil (Total = 3 byte header + panjang descriptor)
        byte[] result = new byte[3 + descriptorLen];

        // Tulis header
        result[0] = (byte) (dto.getVersion() & 0xFF);
        result[1] = (byte) (dto.getNetworkType() & 0xFF);
        result[2] = (byte) (descriptorLen & 0xFF);

        // 4. Copy string bytes ke array hasil (mulai dari index ke-3)
        if (descriptorLen > 0) {
            System.arraycopy(descriptorBytes, 0, result, 3, descriptorLen);
        }

        return result;
    }
}