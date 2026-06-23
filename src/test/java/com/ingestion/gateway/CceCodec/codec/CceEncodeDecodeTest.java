package com.ingestion.gateway.CceCodec.codec;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import com.ingestion.gateway.CceCodec.dto.CceIdentifier;
import com.ingestion.gateway.CceCodec.dto.CceMessageEnvelope;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.*;
import com.ingestion.gateway.CceCodec.dto.impl.byte2.*;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.*;
import com.ingestion.gateway.CceCodec.dto.impl.byteUnfixed.*;
// Asumsi penamaan DTO untuk parameter 0x01, 0x05, dan 0x06

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CceEncodeDecodeTest {

    @Inject
    CceEncoderDispatcher encoderDispatcher;

    @Inject
    CceDecoderDispatcher decoderDispatcher;

    /**
     * Test execution generic untuk CCE yang mengakomodasi struktur Multi-Parameter (List of DTOs)
     * di dalam satu envelope.
     */
    private CceMessageEnvelope executeGenericTest(CceIdentifier identifier, String imei, List<CceDto> originalDtos) throws Exception {
        CceMessageEnvelope originalEnvelope = new CceMessageEnvelope();
        originalEnvelope.setIdentifier(identifier);
        originalEnvelope.setImei(imei);
        originalEnvelope.setCacheRemaining(0);
        originalEnvelope.setNumPackets(1);

        // Memasukkan seluruh parameter 1-byte ke dalam amplop
        originalEnvelope.getParameters().addAll(originalDtos);

        // 1. Encode
        byte[] encodedPacket = encoderDispatcher.encode(originalEnvelope);

        // Validasi strict hasil encode
        assertNotNull(encodedPacket, "Hasil encode tidak boleh null");
        assertTrue(encodedPacket.length > 15, "Ukuran paket biner CCE terlalu pendek untuk dianggap valid");

        // 2. Decode
        CceMessageEnvelope decodedEnvelope = decoderDispatcher.decode(encodedPacket);

        // 3. Verifikasi Envelope dan Header Meta-data
        assertNotNull(decodedEnvelope, "Hasil decode tidak boleh null");
        assertEquals(originalEnvelope.getIdentifier(), decodedEnvelope.getIdentifier(), "Identifier CCE mismatch");
        assertEquals(originalEnvelope.getImei(), decodedEnvelope.getImei(), "IMEI mismatch");
        assertEquals(originalEnvelope.getCacheRemaining(), decodedEnvelope.getCacheRemaining(), "Cache remaining mismatch");
        assertEquals(originalEnvelope.getNumPackets(), decodedEnvelope.getNumPackets(), "Jumlah packet mismatch");

        // Verifikasi dispatcher berhasil membaca persis sejumlah parameter yang dimasukkan
        assertEquals(originalDtos.size(), decodedEnvelope.getParameters().size(), "Jumlah parameter hasil decode tidak sesuai dengan input");

        return decodedEnvelope;
    }

    @Test
    @DisplayName("Test Encode & Decode Multiple 1-Byte Parameters (ID: 0x01, 0x05, 0x06)")
    void testMultipleOneByteParametersEncodeDecode() throws Exception {

        // 1. Setup Data: Parameter ID 0x01 (Event Code)
        EventCodeDto eventCodeDto = new EventCodeDto();
        eventCodeDto.setEventCode((short) 35); // Hex: 0x23

        // 2. Setup Data: Parameter ID 0x05 (GPS Positioning Status)
        GpsPositioningStatusDto gpsStatusDto = new GpsPositioningStatusDto();
        gpsStatusDto.setGpsPositioningStatus((short) 1); // 0x01 (Valid)

        // 3. Setup Data: Parameter ID 0x06 (Number of Satellites)
        NumberOfSatelitesDto satellitesDto = new NumberOfSatelitesDto();
        satellitesDto.setNumberOfSatelites((short) 10); // Hex: 0x0A

        // Kumpulkan dalam satu List untuk dimasukkan ke Envelope
        List<CceDto> originalDtos = Arrays.asList(eventCodeDto, gpsStatusDto, satellitesDto);

        // Eksekusi encode -> decode cycle
        CceMessageEnvelope decodedEnvelope = executeGenericTest(CceIdentifier.PERIODIC_REPORT, "861585040494468", originalDtos);

        // 4. Ekstrak dan Verifikasi nilai internal dari masing-masing DTO hasil decode

        EventCodeDto decodedEventCode = extractDtoFromList(decodedEnvelope, EventCodeDto.class);
        assertNotNull(decodedEventCode, "Parameter ID 0x01 (Event Code) gagal di-decode atau hilang");
        assertEquals(eventCodeDto.getEventCode(), decodedEventCode.getEventCode(), "Nilai Event Code berubah pasca-decode");

        GpsPositioningStatusDto decodedGpsStatus = extractDtoFromList(decodedEnvelope, GpsPositioningStatusDto.class);
        assertNotNull(decodedGpsStatus, "Parameter ID 0x05 (GPS Status) gagal di-decode atau hilang");
        assertEquals(gpsStatusDto.isValid(), decodedGpsStatus.isValid(), "Nilai GPS Status berubah pasca-decode");

        NumberOfSatelitesDto decodedSatellites = extractDtoFromList(decodedEnvelope, NumberOfSatelitesDto.class);
        assertNotNull(decodedSatellites, "Parameter ID 0x06 (Number of Satellites) gagal di-decode atau hilang");
        assertEquals(satellitesDto.getNumberOfSatelites(), decodedSatellites.getNumberOfSatelites(), "Nilai Satellites Count berubah pasca-decode");
    }

    /**
     * Utility method untuk melakukan pencarian DTO secara aman dari List hasil parsing
     */
    private <T extends CceDto> T extractDtoFromList(CceMessageEnvelope envelope, Class<T> targetClass) {
        for (CceDto dto : envelope.getParameters()) {
            if (targetClass.isInstance(dto)) {
                return targetClass.cast(dto);
            }
        }
        return null;
    }
}