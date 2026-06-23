// File: com.ingestion.gateway.CceCoder.encoder.utils.CceEncoderUtils.java
package com.ingestion.gateway.CceCodec.codec.utils;

import java.nio.charset.StandardCharsets;

public class CceEncoderUtils {
    /**
     * Membungkus payload biner dengan Header ASCII dan Checksum Meitrack CCE.
     */
    public static byte[] wrapWithAsciiHeader(char identifier, String imei, byte[] binaryPayload) {
        int dataLength = 1 + imei.length() + 1 + 3 + 1 + binaryPayload.length + 1 + 2 + 2;
        String lenStr = String.valueOf(dataLength);

        StringBuilder sb = new StringBuilder();
        sb.append("$$").append(identifier).append(lenStr)
                .append(",").append(imei).append(",CCE,");

        byte[] headerBytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
        byte[] preChecksum = new byte[headerBytes.length + binaryPayload.length + 1];

        System.arraycopy(headerBytes, 0, preChecksum, 0, headerBytes.length);
        System.arraycopy(binaryPayload, 0, preChecksum, headerBytes.length, binaryPayload.length);
        preChecksum[preChecksum.length - 1] = '*';

        int sum = 0;
        for (byte b : preChecksum) {
            sum += (b & 0xFF);
        }
        String checksumStr = String.format("%02X", sum & 0xFF);

        byte[] finalMsg = new byte[preChecksum.length + 4];
        System.arraycopy(preChecksum, 0, finalMsg, 0, preChecksum.length);

        byte[] tail = (checksumStr + "\r\n").getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(tail, 0, finalMsg, preChecksum.length, tail.length);

        return finalMsg;
    }
}