package com.ingestion.gateway.service;

//import com.ingestion.gateway.Jtt808Coder.decoder.Jtt808DecoderDispatcher;
import com.ingestion.gateway.CceCodec.codec.CceDecoderDispatcher;
import com.ingestion.gateway.dto.RawTrackerMessageDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import jakarta.inject.Inject;

@ApplicationScoped
public class TrackerIngestionService {

    private static final Logger LOG = Logger.getLogger(TrackerIngestionService.class);

    /**
     * Metode utama untuk memproses pesan mentah dari pelacak.
     */
//    @Inject
//    Jtt808DecoderDispatcher jtt808Decoder;
    @Inject
    CceDecoderDispatcher cceDecoder;
    public void processRawMessage(String remoteAddress, String protocol, byte[] rawBytes) {
        // 1. Konversi raw bytes menjadi bentuk Hex string untuk kemudahan observasi
        String hexString = bytesToHex(rawBytes);

        // 2. Bungkus ke dalam DTO
        RawTrackerMessageDto dto = new RawTrackerMessageDto(remoteAddress, protocol, hexString);

        // 3. Log pesan (atau teruskan ke Kafka/Database nantinya)
        LOG.infof("Menerima Data Mentah [%s] dari: %s", protocol, remoteAddress);
        // Memanggil penerjemah
//        jtt808Decoder.decode(rawBytes);
        cceDecoder.decode(rawBytes);
        // TODO: Implementasi un-escape 0x7D 0x02 -> 0x7E
        // TODO: Implementasi validasi Checksum JTT808
        // TODO: Ekstraksi Message Header & Message Body
    }

    /**
     * Fungsi utilitas untuk mengubah byte array menjadi Hex String
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
}