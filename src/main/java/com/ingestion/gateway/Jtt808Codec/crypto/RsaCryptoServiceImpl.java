package com.ingestion.gateway.Jtt808Codec.crypto;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@ApplicationScoped
public class RsaCryptoServiceImpl implements RsaCryptoService {

    private static final Logger LOG = Logger.getLogger(RsaCryptoServiceImpl.class);
    private static final String ALGORITHM = "RSA/ECB/PKCS1Padding"; // Standar padding yang sering dipakai

    // Kunci Private Server (Untuk Dekripsi pesan dari Tracker)
    private PrivateKey serverPrivateKey;

    public RsaCryptoServiceImpl() {
        // TODO: Muat Private Key server Anda di sini saat startup
        // serverPrivateKey = loadPrivateKey(....);
    }

    /**
     * ENKRIPSI (Gunakan Public Key milik Tracker)
     * Dipanggil oleh Jtt808EncoderDispatcher
     */
    public byte[] encrypt(byte[] plainData, byte[] trackerPublicKeyBytes) {
        try {
            PublicKey publicKey = generatePublicKey(trackerPublicKeyBytes);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(plainData);
        } catch (Exception e) {
            LOG.error("Gagal melakukan enkripsi RSA", e);
            return plainData; // Fallback atau lemparkan exception
        }
    }

    /**
     * DEKRIPSI (Gunakan Private Key milik Server)
     * Dipanggil oleh Jtt808DecoderDispatcher
     */
    public byte[] decrypt(byte[] encryptedData) {
        if (serverPrivateKey == null) {
            LOG.warn("Server Private Key belum diinisialisasi!");
            return encryptedData;
        }

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, serverPrivateKey);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            LOG.error("Gagal melakukan dekripsi RSA", e);
            return encryptedData; // Fallback atau lemparkan exception
        }
    }

    // --- Helper Methods ---

    private PublicKey generatePublicKey(byte[] keyBytes) throws Exception {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private PrivateKey generatePrivateKey(byte[] keyBytes) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }
}