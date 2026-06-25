package com.ingestion.gateway.CceCodec.codec;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import com.ingestion.gateway.CceCodec.dto.CceIdentifier;
import com.ingestion.gateway.CceCodec.dto.CceMessageEnvelope;
import com.ingestion.gateway.CceCodec.dto.impl.byte1.*;
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

        EventCodeDto eventCodeDto = new EventCodeDto();
        eventCodeDto.setParameterId(0x01);
        eventCodeDto.setEventCode((short) 35); // Hex: 0x23

        GpsPositioningStatusDto gpsStatusDto = new GpsPositioningStatusDto();
        gpsStatusDto.setParameterId(0x05);
        gpsStatusDto.setGpsPositioningStatus((short) 1); // 0x01 (Valid)

        NumberOfSatelitesDto satellitesDto = new NumberOfSatelitesDto();
        satellitesDto.setParameterId(0x06);
        satellitesDto.setNumberOfSatelites((short) 10); // Hex: 0x0A

        GsmSignalStrengthDto gsmSignalStrengthDto = new GsmSignalStrengthDto();
        gsmSignalStrengthDto.setParameterId(0x07);
        gsmSignalStrengthDto.setGsmSignalStrength((short) 28); // Hex: 0x1C

        OutputPortStatusDto outputPortStatusDto = new OutputPortStatusDto();
        outputPortStatusDto.setParameterId(0x14);
        outputPortStatusDto.setOutputPortStatus((short) 0b00001010); // port 2 and 4 active

        InputPortStatusDto inputPortStatusDto = new InputPortStatusDto();
        inputPortStatusDto.setParameterId(0x15);
        inputPortStatusDto.setInputPortStatus((short) 0b00000101); // port 1 and 3 active

        GeoFenceNumberDto geoFenceNumberDto = new GeoFenceNumberDto();
        geoFenceNumberDto.setParameterId(0x1b);
        geoFenceNumberDto.setGeoFenceNumber((short)0);

        TemperatureSensorNoDto tempSensorNoDto = new TemperatureSensorNoDto();
        tempSensorNoDto.setParameterId(0x27);
        tempSensorNoDto.setTemperatureSensorNo((short)0x7);

        DeadReckoningStateDto deadReckoningStateDto = new DeadReckoningStateDto();
        deadReckoningStateDto.setParameterId(0x5b);
        deadReckoningStateDto.setDeadReckoningState((short)0x1);

        ClutchSwitchDto clutchSwitchDto = new ClutchSwitchDto();
        clutchSwitchDto.setParameterId(0x93);
        clutchSwitchDto.setClutchSwitch((short)0x1);

        TachographPerformanceDto tachoPerformanceDto = new TachographPerformanceDto();
        tachoPerformanceDto.setParameterId(0x94);
        tachoPerformanceDto.setTachographPerformance((short)0x1);

        ParkingBrakeSwitchDto parkingBrakeSwitchDto = new ParkingBrakeSwitchDto();
        parkingBrakeSwitchDto.setParameterId(0x95);
        parkingBrakeSwitchDto.setParkingBrakeSwitch((short)0x1);

        CruiseControlSystemDto cruiseControlSystemDto = new CruiseControlSystemDto();
        cruiseControlSystemDto.setParameterId(0x96);
        cruiseControlSystemDto.setCruiseControlSystem((short)0x1);

        // Kumpulkan dalam satu List untuk dimasukkan ke Envelope
        List<CceDto> originalDtos = Arrays.asList(
                eventCodeDto,
                gpsStatusDto,
                satellitesDto,
                gsmSignalStrengthDto,
                outputPortStatusDto,
                inputPortStatusDto,
                geoFenceNumberDto,
                tempSensorNoDto,
                deadReckoningStateDto,
                clutchSwitchDto,
                tachoPerformanceDto,
                parkingBrakeSwitchDto,
                cruiseControlSystemDto
        );

        // Eksekusi encode -> decode cycle
        CceMessageEnvelope decodedEnvelope = executeGenericTest(CceIdentifier.PERIODIC_REPORT, "861585040494468", originalDtos);


        EventCodeDto decodedEventCode = extractDtoFromList(decodedEnvelope, EventCodeDto.class);
        assertNotNull(decodedEventCode, "Parameter ID 0x01 gagal di-decode atau hilang");
        assertEquals(eventCodeDto.getEventCode(), decodedEventCode.getEventCode(), "Nilai Event Code berubah pasca-decode");

        GpsPositioningStatusDto decodedGpsStatus = extractDtoFromList(decodedEnvelope, GpsPositioningStatusDto.class);
        assertNotNull(decodedGpsStatus, "Parameter ID 0x05 gagal di-decode atau hilang");
        assertEquals(gpsStatusDto.isValid(), decodedGpsStatus.isValid(), "Nilai GPS Status berubah pasca-decode");

        NumberOfSatelitesDto decodedSatellites = extractDtoFromList(decodedEnvelope, NumberOfSatelitesDto.class);
        assertNotNull(decodedSatellites, String.format("Parameter ID 0x%04X gagal di-decode atau hilang",satellitesDto.getParameterId()));
        assertEquals(satellitesDto.getNumberOfSatelites(), decodedSatellites.getNumberOfSatelites(), "Nilai berubah pasca-decode");

        GsmSignalStrengthDto decodedGsmSignalStr = extractDtoFromList(decodedEnvelope, GsmSignalStrengthDto.class);
        assertNotNull(decodedGsmSignalStr, String.format("Parameter ID 0x%04X gagal di-decode atau hilang",gsmSignalStrengthDto.getParameterId()));
        assertEquals(gsmSignalStrengthDto.getGsmSignalStrength(), decodedGsmSignalStr.getGsmSignalStrength(), "Nilai berubah pasca-decode");

        OutputPortStatusDto decodedOutputPortStatus = extractDtoFromList(decodedEnvelope, OutputPortStatusDto.class);
        assertNotNull(decodedOutputPortStatus, String.format("Parameter ID 0x%04X gagal di-decode atau hilang",outputPortStatusDto.getParameterId()));
        assertEquals(outputPortStatusDto.getOutputPortStatus(), decodedOutputPortStatus.getOutputPortStatus(), "Nilai berubah pasca-decode");

        InputPortStatusDto decodedInputPortStatus = extractDtoFromList(decodedEnvelope, InputPortStatusDto.class);
        assertNotNull(decodedInputPortStatus, String.format("Parameter ID 0x%04X gagal di-decode atau hilang",inputPortStatusDto.getParameterId()));
        assertEquals(inputPortStatusDto.getInputPortStatus(), decodedInputPortStatus.getInputPortStatus(), "Nilai berubah pasca-decode");

        GeoFenceNumberDto decodedGeoFenceNumber = extractDtoFromList(decodedEnvelope, GeoFenceNumberDto.class);
        assertNotNull(decodedGeoFenceNumber, String.format("Parameter ID 0x%04X gagal di-decode atau hilang",geoFenceNumberDto.getParameterId()));
        assertEquals(geoFenceNumberDto.getGeoFenceNumber(), decodedGeoFenceNumber.getGeoFenceNumber(), "Nilai berubah pasca-decode");

        TemperatureSensorNoDto decodedTempSensorNo = extractDtoFromList(decodedEnvelope, TemperatureSensorNoDto.class);
        assertNotNull(decodedTempSensorNo, String.format("Parameter ID 0x%04X gagal di-decode atau hilang",tempSensorNoDto.getParameterId()));
        assertEquals(tempSensorNoDto.getTemperatureSensorNo(), decodedTempSensorNo.getTemperatureSensorNo(), "Nilai berubah pasca-decode");

        DeadReckoningStateDto decodedDeadReckoningState = extractDtoFromList(decodedEnvelope, DeadReckoningStateDto.class);
        assertNotNull(decodedDeadReckoningState, String.format("Parameter ID 0x%04X gagal di-decode atau hilang",deadReckoningStateDto.getParameterId()));
        assertEquals(deadReckoningStateDto.getDeadReckoningState(), decodedDeadReckoningState.getDeadReckoningState(), "Nilai berubah pasca-decode");

        ClutchSwitchDto decodedClutchSwitch = extractDtoFromList(decodedEnvelope, ClutchSwitchDto.class);
        assertNotNull(decodedClutchSwitch, String.format("Parameter ID 0x%04X gagal di-decode atau hilang",clutchSwitchDto.getParameterId()));
        assertEquals(clutchSwitchDto.isPressed(), decodedClutchSwitch.isPressed(), "Nilai berubah pasca-decode");

        TachographPerformanceDto decodedTachoPerformanceAnalysis = extractDtoFromList(decodedEnvelope, TachographPerformanceDto.class);
        assertNotNull(decodedTachoPerformanceAnalysis, String.format("Parameter ID 0x%04X gagal di-decode atau hilang",tachoPerformanceDto.getParameterId()));
        assertEquals(tachoPerformanceDto.isPerformanceAnalysis(), decodedTachoPerformanceAnalysis.isPerformanceAnalysis(), "Nilai berubah pasca-decode");

        ParkingBrakeSwitchDto decodedParkingBrakeSwitch = extractDtoFromList(decodedEnvelope, ParkingBrakeSwitchDto.class);
        assertNotNull(decodedParkingBrakeSwitch, String.format("Parameter ID 0x%04X gagal di-decode atau hilang",parkingBrakeSwitchDto.getParameterId()));
        assertEquals(parkingBrakeSwitchDto.isApplyBrake(), decodedParkingBrakeSwitch.isApplyBrake(), "Nilai berubah pasca-decode");




        System.out.println(originalDtos.size());
        //        assertEquals(1,2);
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