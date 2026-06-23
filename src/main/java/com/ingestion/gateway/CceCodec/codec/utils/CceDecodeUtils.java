package com.ingestion.gateway.CceCodec.codec.utils;

public class CceDecodeUtils {

    private CceDecodeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Mengkonversi Hex String (seperti "4909040100000000") menjadi byte array.
     */
    public static byte[] hexStringToByteArray(String s) {
        s = s.replaceAll("\\s+", ""); // Hilangkan spasi jika ada
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    /**
     * Membaca 4 byte dari array menjadi integer (Little-Endian)
     * Sesuai instruksi di dokumen: "01 00 00 00 is the little endian byte order"
     */
    public static int readIntLittleEndian(byte[] data, int offset) {
        return (data[offset] & 0xFF) |
                ((data[offset + 1] & 0xFF) << 8) |
                ((data[offset + 2] & 0xFF) << 16) |
                ((data[offset + 3] & 0xFF) << 24);
    }
}