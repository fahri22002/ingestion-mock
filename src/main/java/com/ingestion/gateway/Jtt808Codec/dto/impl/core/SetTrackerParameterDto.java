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
public class SetTrackerParameterDto implements Jtt808Dto {

    // BYTE (1 byte): Total number of parameter items
    private int totalParameters;

    // Parameter Item List
    private List<ParameterItem> items = new ArrayList<>();

    public void addParameter(long parameterId, long dwordValue) {
        byte[] valueBytes = ByteBuffer.allocate(4).putInt((int) dwordValue).array();
        this.items.add(new ParameterItem(parameterId, 4, valueBytes));
        this.totalParameters = this.items.size();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParameterItem {
        // DWORD (4 byte): Parameter ID (e.g., 0x0029, 0x0030)
        private long parameterId;

        // BYTE (1 byte): Parameter Length
        private int parameterLength;

        // BYTE[n]: Dynamic Parameter Value
        private byte[] parameterValue;

        // --- Helper Methods untuk Memudahkan Service Layer Ekstraksi Nilai ---

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