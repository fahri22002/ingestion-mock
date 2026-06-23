package com.ingestion.gateway.CceCodec.codec;

import com.ingestion.gateway.CceCodec.dto.CceDto;

public interface CceMessageCodec<T extends CceDto> {

    // --- Meta Informasi Parameter ---
    Class<T> getSupportedDtoClass();
    int getSupportedParameterId();
    int getByteCategory(); // 1, 2, 4, atau -1 (Unfixed)
    String getParameterName();

    // --- Logika Parsing & Assembling ---
    T decodeBody(byte[] bodyData);
    byte[] encodeValue(T dto);
}