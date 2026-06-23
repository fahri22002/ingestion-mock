package com.ingestion.gateway.Jtt808Codec.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jtt808MessageEnvelope {

    // ID Pesan JTT808 (Misal: 0x0200, 0x8103)
    private int messageId;

    // Nomor IMEI atau Nomor Telepon Terminal
    // (Versi 2019 menggunakan BCD 10 byte, Versi lama menggunakan 6 byte)
    private String imei;

    // Nomor Seri Pesan
    private int serialNumber;

    // Versi Protokol (Jika properti pesan bit-14 diset 1)
    private int protocolVersion = 1;

    // --- REPRESENTASI MESSAGE BODY PROPERTY ---
    // Metode Enkripsi (Bit 10-12). 0: Tidak dienkripsi, 1: RSA
    private int encryptionMethod = 0;

    // Informasi Segmentasi (Jika pesan panjang)
    private boolean isSegmented = false;
    private int totalPackets = 1;
    private int packetSequence = 1;
    // ------------------------------------------

    // DTO Body yang spesifik (Bisa null untuk perintah yang tidak memiliki body)
    private Jtt808Dto data;
}