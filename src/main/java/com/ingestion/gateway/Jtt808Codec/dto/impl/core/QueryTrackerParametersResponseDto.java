package com.ingestion.gateway.Jtt808Codec.dto.impl.core;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Data
public class QueryTrackerParametersResponseDto implements Jtt808Dto {

    // WORD (2 byte): Response Serial Number
    private int responseSerialNumber;

    // BYTE (1 byte): Number of response parameters
    private int totalParameters;

    // Parameter Item List [cite: 118]
    private List<ParameterItem> items = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterItem {
        // DWORD (4 byte): Parameter ID [cite: 109]
        private long parameterId;

        // BYTE (1 byte): Parameter Length [cite: 109]
        private int parameterLength;

        // BYTE[n]: Parameter Value [cite: 109]
        private byte[] parameterValue;

        // --- Helper Methods untuk Memudahkan Ekstraksi Nilai ---
        public int getValueAsByte() {
            return parameterValue != null && parameterValue.length >= 1 ? Byte.toUnsignedInt(parameterValue[0]) : 0;
        }

        public int getValueAsWord() {
            if (parameterValue == null || parameterValue.length < 2) return 0;
            return ByteBuffer.wrap(parameterValue).getShort() & 0xFFFF;
        }

        public long getValueAsDword() {
            if (parameterValue == null || parameterValue.length < 4) return 0;
            return Integer.toUnsignedLong(ByteBuffer.wrap(parameterValue).getInt());
        }

        public String getValueAsString() {
            if (parameterValue == null) return "";
            return new String(parameterValue, Charset.forName("GBK")).trim();
        }
    }
}