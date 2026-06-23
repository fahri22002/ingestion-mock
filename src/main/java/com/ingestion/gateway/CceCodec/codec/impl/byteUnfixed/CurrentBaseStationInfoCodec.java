package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByteUnfixedCodec;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.CurrentBaseStationInfoDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CurrentBaseStationInfoCodec extends AbstractByteUnfixedCodec<CurrentBaseStationInfoDto> {

    @Override
    public Class<CurrentBaseStationInfoDto> getSupportedDtoClass() {
        return CurrentBaseStationInfoDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0x0E;
    }

    @Override
    public String getParameterName() {
        return "Current base station info";
    }

    @Override
    protected CurrentBaseStationInfoDto mapToDto(byte[] decodedValueBytes) {
        if (decodedValueBytes == null || decodedValueBytes.length < 12) {
            return null;
        }

        CurrentBaseStationInfoDto dto = new CurrentBaseStationInfoDto();

        // 1. MCC: 16-bit unsigned
        dto.setMcc((decodedValueBytes[0] & 0xFF) | ((decodedValueBytes[1] & 0xFF) << 8));

        // 2. MNC: 16-bit unsigned;
        dto.setMnc((decodedValueBytes[2] & 0xFF) | ((decodedValueBytes[3] & 0xFF) << 8));

        // 3. LAC: 16-bit unsigned;
        dto.setLac((decodedValueBytes[4] & 0xFF) | ((decodedValueBytes[5] & 0xFF) << 8));

        // 4. CELL_ID: 32-bit unsigned;
        long cellId = (decodedValueBytes[6] & 0xFFL) |
                ((decodedValueBytes[7] & 0xFFL) << 8) |
                ((decodedValueBytes[8] & 0xFFL) << 16) |
                ((decodedValueBytes[9] & 0xFFL) << 24);
        dto.setCellId(cellId);

        // 5. RX_LEVEL: 16-bit signed;
        // Casting langsung ke (short) akan otomatis mempertahankan nilai minus
        short rxLevel = (short) ((decodedValueBytes[10] & 0xFF) | (decodedValueBytes[11] << 8));
        dto.setRxLevel(rxLevel);

        return dto;
    }

    @Override
    protected byte[] mapFromDto(CurrentBaseStationInfoDto dto) {
        byte[] bytes = new byte[12]; // Sesuai spesifikasi panjangnya 12 byte

        // 1. MCC (2 bytes)
        int mcc = dto.getMcc();
        bytes[0] = (byte) (mcc & 0xFF);
        bytes[1] = (byte) ((mcc >> 8) & 0xFF);

        // 2. MNC (2 bytes)
        int mnc = dto.getMnc();
        bytes[2] = (byte) (mnc & 0xFF);
        bytes[3] = (byte) ((mnc >> 8) & 0xFF);

        // 3. LAC (2 bytes)
        int lac = dto.getLac();
        bytes[4] = (byte) (lac & 0xFF);
        bytes[5] = (byte) ((lac >> 8) & 0xFF);

        // 4. CELL_ID (4 bytes)
        long cellId = dto.getCellId();
        bytes[6] = (byte) (cellId & 0xFF);
        bytes[7] = (byte) ((cellId >> 8) & 0xFF);
        bytes[8] = (byte) ((cellId >> 16) & 0xFF);
        bytes[9] = (byte) ((cellId >> 24) & 0xFF);

        // 5. RX_LEVEL (2 bytes)
        short rxLevel = dto.getRxLevel();
        bytes[10] = (byte) (rxLevel & 0xFF);
        bytes[11] = (byte) ((rxLevel >> 8) & 0xFF);

        return bytes;
    }
}