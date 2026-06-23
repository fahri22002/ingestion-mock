package com.ingestion.gateway.Jtt808Codec.codec;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;

public interface Jtt808MessageCodec<T extends Jtt808Dto> {
    // Menentukan Message ID apa yang ditangani oleh class ini
    int getSupportedMessageId();
    Class<T> getSupportedDtoClass();

    String getCommandName();

    // --- Logika Parsing & Assembling ---
    T decodeBody(byte[] bodyData);
    byte[] encodeValue(T dto);
}