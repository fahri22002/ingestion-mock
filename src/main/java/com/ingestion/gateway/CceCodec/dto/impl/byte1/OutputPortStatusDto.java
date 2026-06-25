package com.ingestion.gateway.CceCodec.dto.impl.byte1;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import lombok.Data;

@Data
public class OutputPortStatusDto implements CceDto{
    private int parameterId;
	private short outputPortStatus;

    public boolean isActive(int portNumber) {
        if (portNumber < 1 || portNumber > 8) {
            throw new IllegalArgumentException("Output port harus antara 1 sampai 8");
        }

        // Membuat bitmask dengan Left Shift
        int mask = 1 << (portNumber - 1);

        // Gunakan Bitwise AND '&' untuk mengecek apakah bit pada posisi tersebut adalah 1
        return (outputPortStatus & mask) != 0;
    }
}
