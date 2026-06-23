package com.ingestion.gateway.Jtt808Codec.crypto;

public interface RsaCryptoService {
    public byte[] decrypt(byte[] plainData);
    public byte[] encrypt(byte[] plainData, byte[] trackerPublicKeyBytes);
}
