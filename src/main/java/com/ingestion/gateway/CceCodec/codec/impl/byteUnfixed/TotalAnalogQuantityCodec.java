// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.TotalAnalogQuantityCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.TotalAnalogQuantityDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class TotalAnalogQuantityCodec extends AbstractByteUnfixedCodec<TotalAnalogQuantityDto> {

    private static final Logger LOG = Logger.getLogger(TotalAnalogQuantityCodec.class);

    @Override
    public Class<TotalAnalogQuantityDto> getSupportedDtoClass() {
        return TotalAnalogQuantityDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x4C;
    }

    @Override
    public String getParameterName() {
        return "Total analog quantity";
    }

    @Override
    protected TotalAnalogQuantityDto mapToDto(byte[] decodedValueBytes) {
        // Validasi minimal 1 byte (variabel 'number')
        if (decodedValueBytes == null || decodedValueBytes.length < 1) {
            return null;
        }

        TotalAnalogQuantityDto dto = new TotalAnalogQuantityDto();
        try {
            // 1. Baca jumlah data analog yang dikirim
            int number = decodedValueBytes[0] & 0xFF;

            // Batasi jumlah maksimal sesuai dokumen (32) untuk menghindari loop tak berujung/error offset
            number = Math.min(number, 32);

            // Validasi sisa ukuran array: (number * 3 byte per item) + 1 byte di awal
            if (decodedValueBytes.length < 1 + (number * 3)) {
                LOG.warnf("Panjang data 0x4C tidak sesuai. Diharapkan minimal %d, aktual: %d", 1 + (number * 3), decodedValueBytes.length);
                return null;
            }

            // 2. Looping sebanyak 'number' untuk mengekstrak data
            for (int i = 0; i < number; i++) {
                // Hitung titik awal pembacaan (offset) untuk item ke-i
                int offset = 1 + (i * 3);

                // Ekstrak AD Number (1 byte)
                int adNumber = decodedValueBytes[offset] & 0xFF;

                // Ekstrak Voltage (2 byte, Little-Endian)
                int voltageMv = (decodedValueBytes[offset + 1] & 0xFF) |
                        ((decodedValueBytes[offset + 2] & 0xFF) << 8);

                // Tambahkan ke List di dalam DTO
                dto.getAnalogItems().add(new TotalAnalogQuantityDto.AnalogItem(adNumber, voltageMv));
            }

            return dto;
        } catch (Exception e) {
            LOG.error("Gagal melakukan parsing nilai TotalAnalogQuantity", e);
            return null;
        }
    }

    @Override
    protected byte[] mapFromDto(TotalAnalogQuantityDto dto) {
        // Pastikan list tidak null
        List<TotalAnalogQuantityDto.AnalogItem> items = dto.getAnalogItems();
        if (items == null) {
            return new byte[]{0}; // Jika kosong, kembalikan 'number' = 0
        }

        // Batasi maksimal 32 item sesuai spesifikasi
        int number = Math.min(items.size(), 32);

        // Siapkan array hasil (1 byte untuk 'number' + (3 byte * jumlah item))
        byte[] result = new byte[1 + (number * 3)];

        // Tulis jumlah item di index 0
        result[0] = (byte) (number & 0xFF);

        // Looping untuk menulis masing-masing item
        for (int i = 0; i < number; i++) {
            TotalAnalogQuantityDto.AnalogItem item = items.get(i);
            int offset = 1 + (i * 3);

            // Tulis AD Number (1 byte)
            result[offset] = (byte) (item.getAdNumber() & 0xFF);

            // Tulis Voltage dalam format Little-Endian (2 byte)
            int voltage = item.getVoltageMv();
            result[offset + 1] = (byte) (voltage & 0xFF);           // Low byte
            result[offset + 2] = (byte) ((voltage >> 8) & 0xFF);    // High byte
        }

        return result;
    }
}