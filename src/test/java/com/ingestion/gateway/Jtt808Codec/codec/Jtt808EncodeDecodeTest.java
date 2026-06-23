package com.ingestion.gateway.Jtt808Codec.codec;

import com.ingestion.gateway.Jtt808Codec.dto.Jtt808Dto;
import com.ingestion.gateway.Jtt808Codec.dto.Jtt808MessageEnvelope;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.*;
import com.ingestion.gateway.Jtt808Codec.codec.impl.FileDataUploadCodec;
import com.ingestion.gateway.Jtt808Codec.codec.impl.RealTimeAudioAndVideoStreamCodec;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

@QuarkusTest
public class Jtt808EncodeDecodeTest {

    @Inject
    Jtt808EncoderDispatcher encoderDispatcher;

    @Inject
    Jtt808DecoderDispatcher decoderDispatcher;

    private <T extends Jtt808Dto> T executeGenericTest(int messageId, String imei, int serialNumber, T originalDto, Class<T> dtoClass) throws Exception {
        Jtt808MessageEnvelope originalEnvelope = new Jtt808MessageEnvelope();
        originalEnvelope.setMessageId(messageId);
        originalEnvelope.setImei(imei);
        originalEnvelope.setSerialNumber(serialNumber);
        originalEnvelope.setProtocolVersion(1);
        originalEnvelope.setSegmented(false);
        originalEnvelope.setEncryptionMethod(0);
        originalEnvelope.setData(originalDto);

        List<byte[]> encodedPackets = encoderDispatcher.encode(originalEnvelope);
        assertFalse(encodedPackets.isEmpty(), "Hasil encode tidak boleh kosong");
        assertTrue(encodedPackets.get(0).length > 12);

        Jtt808MessageEnvelope finalDecodedEnvelope = null;

        // PERUBAHAN: Loop dan Decode semua paket (mensimulasikan paket datang satu per satu)
        for (byte[] packet : encodedPackets) {
            assertEquals(0x7E, packet[0]);
            assertEquals(0x7E, packet[packet.length - 1]);
            finalDecodedEnvelope = decoderDispatcher.decode(packet);
        }

        assertNotNull(finalDecodedEnvelope);
        assertNotNull(finalDecodedEnvelope.getData());


        assertNotNull(finalDecodedEnvelope);
        assertEquals(originalEnvelope.getMessageId(), finalDecodedEnvelope.getMessageId());
        assertEquals(originalEnvelope.getImei(), finalDecodedEnvelope.getImei());
        assertEquals(originalEnvelope.getSerialNumber(), finalDecodedEnvelope.getSerialNumber());
        assertEquals(originalEnvelope.getProtocolVersion(), finalDecodedEnvelope.getProtocolVersion());

        assertNotNull(finalDecodedEnvelope.getData());
        assertTrue(dtoClass.isInstance(finalDecodedEnvelope.getData()));

        return dtoClass.cast(finalDecodedEnvelope.getData());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x0001)")
    void testTrackerGeneralResponseEncodeDecode() throws Exception {
        TrackerGeneralResponseDto originalDto = new TrackerGeneralResponseDto();
        originalDto.setResponseSerialNumber(0x7E7D);
        originalDto.setResponseID(0x8001);
        originalDto.setResult(0);

        TrackerGeneralResponseDto decodedDto = executeGenericTest(0x0001, "34567890123456789", 5555, originalDto, TrackerGeneralResponseDto.class);

        assertEquals(originalDto.getResponseSerialNumber(), decodedDto.getResponseSerialNumber());
        assertEquals(originalDto.getResponseID(), decodedDto.getResponseID());
        assertEquals(originalDto.getResult(), decodedDto.getResult());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x8001)")
    void testPlatformGeneralResponseEncodeDecode() throws Exception {
        PlatformGeneralResponseDto originalDto = new PlatformGeneralResponseDto();
        originalDto.setResponseSerialNumber(0x777D);
        originalDto.setResponseID(0x0001);
        originalDto.setResult(1);

        PlatformGeneralResponseDto decodedDto = executeGenericTest(0x8001, "345678901236789", 5775, originalDto, PlatformGeneralResponseDto.class);

        assertEquals(originalDto.getResponseSerialNumber(), decodedDto.getResponseSerialNumber());
        assertEquals(originalDto.getResponseID(), decodedDto.getResponseID());
        assertEquals(originalDto.getResult(), decodedDto.getResult());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x0100)")
    void testTrackerRegistrationEncodeDecode() throws Exception {
        TrackerRegistrationDto originalDto = new TrackerRegistrationDto();
        originalDto.setProvinceId(0x777D);
        originalDto.setCityAndCountryId(0x727D);
        originalDto.setManufacturerId("abcd");
        originalDto.setTerminalModel("model 1");
        originalDto.setTerminalModelId("modelid");
        originalDto.setLicensePlateColor(2);
        originalDto.setVehicleIdentification("B 1234 CD");

        TrackerRegistrationDto decodedDto = executeGenericTest(0x0100, "345678901236789", 5775, originalDto, TrackerRegistrationDto.class);

        assertEquals(originalDto.getProvinceId(), decodedDto.getProvinceId());
        assertEquals(originalDto.getCityAndCountryId(), decodedDto.getCityAndCountryId());
        assertEquals(originalDto.getManufacturerId(), decodedDto.getManufacturerId());
        assertEquals(originalDto.getTerminalModel(), decodedDto.getTerminalModel());
        assertEquals(originalDto.getTerminalModelId(), decodedDto.getTerminalModelId());
        assertEquals(originalDto.getLicensePlateColor(), decodedDto.getLicensePlateColor());
        assertEquals(originalDto.getVehicleIdentification(), decodedDto.getVehicleIdentification());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x8100)")
    void testTrackerRegistrationResponseEncodeDecode() throws Exception {
        TrackerRegistrationResponseDto originalDto = new TrackerRegistrationResponseDto();
        originalDto.setResponseSerialNumber(0x777D);
        originalDto.setResult(0);
        originalDto.setAuthCode("AUTH1234567899092842084028");

        TrackerRegistrationResponseDto decodedDto = executeGenericTest(0x8100, "345678901236789", 5775, originalDto, TrackerRegistrationResponseDto.class);

        assertEquals(originalDto.getResponseSerialNumber(), decodedDto.getResponseSerialNumber());
        assertEquals(originalDto.getResult(), decodedDto.getResult());
        assertEquals(originalDto.getAuthCode(), decodedDto.getAuthCode());

        // Negative
        TrackerRegistrationResponseDto originalDto1 = new TrackerRegistrationResponseDto();
        originalDto1.setResponseSerialNumber(0x777D);
        originalDto1.setResult(1);
        originalDto1.setAuthCode("AUTH1234567899092842084028");

        TrackerRegistrationResponseDto decodedDto1 = executeGenericTest(0x8100, "345678901236789", 5775, originalDto1, TrackerRegistrationResponseDto.class);

        assertEquals(originalDto1.getResponseSerialNumber(), decodedDto1.getResponseSerialNumber());
        assertEquals(originalDto1.getResult(), decodedDto1.getResult());
        assertEquals("", decodedDto1.getAuthCode());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x0102)")
    void testTrackerAuthenticationEncodeDecode() throws Exception {
        TrackerAuthenticationDto originalDto = new TrackerAuthenticationDto();
        originalDto.setAuthCodeLength(7);
        originalDto.setAuthCode("1234567");
        originalDto.setTrackerImei("12345678912");
        originalDto.setSoftwareVersionNumber("Version 12.3.81");

        TrackerAuthenticationDto decodedDto = executeGenericTest(0x0102, "345678901236789", 5775, originalDto, TrackerAuthenticationDto.class);

        assertEquals(originalDto.getAuthCodeLength(), decodedDto.getAuthCodeLength());
        assertEquals(originalDto.getAuthCode(), decodedDto.getAuthCode());
        assertEquals(originalDto.getTrackerImei(), decodedDto.getTrackerImei());
        assertEquals(originalDto.getSoftwareVersionNumber(), decodedDto.getSoftwareVersionNumber());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x8103)")
    void testSetTrackerParameterEncodeDecodeSegmented() throws Exception {
        SetTrackerParameterDto originalDto = new SetTrackerParameterDto();
        List<SetTrackerParameterDto.ParameterItem> items = new ArrayList<>();

        for (int i = 0; i < 200; i++) {
            SetTrackerParameterDto.ParameterItem item = new SetTrackerParameterDto.ParameterItem();
            item.setParameterId(0x0001 + i);

            byte[] dummyValue = String.format("VALUE-%04d", i).getBytes();
            item.setParameterLength(dummyValue.length);
            item.setParameterValue(dummyValue);

            items.add(item);
        }

        originalDto.setTotalParameters(items.size());
        originalDto.setItems(items);

        SetTrackerParameterDto decodedDto = executeGenericTest(0x8103, "34567890123456789", 1122, originalDto, SetTrackerParameterDto.class);

        assertEquals(originalDto.getTotalParameters(), decodedDto.getTotalParameters());
        assertEquals(200, decodedDto.getItems().size());

        SetTrackerParameterDto.ParameterItem lastOriginal = originalDto.getItems().get(199);
        SetTrackerParameterDto.ParameterItem lastDecoded = decodedDto.getItems().get(199);

        assertEquals(lastOriginal.getParameterId(), lastDecoded.getParameterId());
        assertEquals(lastOriginal.getParameterLength(), lastDecoded.getParameterLength());
        assertArrayEquals(lastOriginal.getParameterValue(), lastDecoded.getParameterValue());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x8104)")
    void testQueryTrackerParametersEncodeDecode() throws Exception {
        QueryTrackerParametersDto originalDto = new QueryTrackerParametersDto();

        QueryTrackerParametersDto decodedDto = executeGenericTest(
                0x8104,
                "34567890123456789",
                7777,
                originalDto,
                QueryTrackerParametersDto.class
        );
        assertNotNull(decodedDto, "DTO hasil decode tidak boleh null");
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x8106)")
    void testQuerySpecifiedTrackerParametersEncodeDecode() throws Exception {
        QuerySpecifiedTrackerParametersDto originalDto = new QuerySpecifiedTrackerParametersDto();
        List<Long> ids = new ArrayList<>();
        ids.add(0x0001L);
        ids.add(0x0013L);
        ids.add(0x0055L);

        originalDto.setTotalParameters(ids.size());
        originalDto.setParameterIds(ids);

        QuerySpecifiedTrackerParametersDto decodedDto = executeGenericTest(0x8106, "34567890123456789", 1122, originalDto, QuerySpecifiedTrackerParametersDto.class);

        assertEquals(originalDto.getTotalParameters(), decodedDto.getTotalParameters());
        assertEquals(originalDto.getParameterIds().size(), decodedDto.getParameterIds().size());
        for (int i = 0; i < ids.size(); i++) {
            assertEquals(originalDto.getParameterIds().get(i), decodedDto.getParameterIds().get(i));
        }
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x0104)")
    void testQueryTrackerParametersResponseEncodeDecode() throws Exception {
        QueryTrackerParametersResponseDto originalDto = new QueryTrackerParametersResponseDto();
        originalDto.setResponseSerialNumber(0x4321);

        List<QueryTrackerParametersResponseDto.ParameterItem> items = new ArrayList<>();

        QueryTrackerParametersResponseDto.ParameterItem item1 = new QueryTrackerParametersResponseDto.ParameterItem();
        item1.setParameterId(0x0001L);
        item1.setParameterLength(4);
        item1.setParameterValue(new byte[]{0x00, 0x00, 0x00, 0x0A});
        items.add(item1);

        QueryTrackerParametersResponseDto.ParameterItem item2 = new QueryTrackerParametersResponseDto.ParameterItem();
        item2.setParameterId(0x0013L);
        byte[] stringVal = "apn.provider.com".getBytes();
        item2.setParameterLength(stringVal.length);
        item2.setParameterValue(stringVal);
        items.add(item2);

        originalDto.setTotalParameters(items.size());
        originalDto.setItems(items);

        QueryTrackerParametersResponseDto decodedDto = executeGenericTest(0x0104, "34567890123456789", 1122, originalDto, QueryTrackerParametersResponseDto.class);

        assertEquals(originalDto.getResponseSerialNumber(), decodedDto.getResponseSerialNumber());
        assertEquals(originalDto.getTotalParameters(), decodedDto.getTotalParameters());
        assertEquals(originalDto.getItems().size(), decodedDto.getItems().size());

        for (int i = 0; i < items.size(); i++) {
            assertEquals(originalDto.getItems().get(i).getParameterId(), decodedDto.getItems().get(i).getParameterId());
            assertEquals(originalDto.getItems().get(i).getParameterLength(), decodedDto.getItems().get(i).getParameterLength());
            assertArrayEquals(originalDto.getItems().get(i).getParameterValue(), decodedDto.getItems().get(i).getParameterValue());
        }
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x0200)")
    void testLocationInformationReportEncodeDecode() throws Exception {
        LocationInformationReportDto originalDto = new LocationInformationReportDto();
        originalDto.setAlarmFlag(0x00000001L);
        originalDto.setStatus(0x00000002L);
        originalDto.setLatitude(22334455L);
        originalDto.setLongitude(11334455L);
        originalDto.setElevation(150);
        originalDto.setSpeed(65);
        originalDto.setDirection(90);
        originalDto.setTime("260623123000");

        List<LocationInformationReportDto.AdditionalItem> items = new ArrayList<>();

        LocationInformationReportDto.AdditionalItem item1 = new LocationInformationReportDto.AdditionalItem();
        item1.setId(0x01);
        item1.setLength(4);
        item1.setRawValue(new byte[]{0x00, 0x00, 0x01, 0x2C});
        items.add(item1);

        LocationInformationReportDto.AdditionalItem item2 = new LocationInformationReportDto.AdditionalItem();
        item2.setId(0x64);
        item2.setLength(47);

        LocationInformationReportDto.AdasAlarmInfo adas = new LocationInformationReportDto.AdasAlarmInfo();
        adas.setAlarmId(999L);
        adas.setFlagStatus(1);
        adas.setAlarmEventType(2);
        adas.setAlarmLevel(3);
        adas.setPrecedingVehicleSpeed(60);
        adas.setDistanceToPreceding(10);
        adas.setDeviationType(0);
        adas.setRoadSignType(1);
        adas.setRoadSignData(80);
        adas.setVehicleSpeed(65);
        adas.setElevation(150);
        adas.setLatitude(22334455L);
        adas.setLongitude(11334455L);
        adas.setDateTime("260623123000");
        adas.setVehicleStatus(1);

        LocationInformationReportDto.AlarmIdentificationNumber alarmIdNum = new LocationInformationReportDto.AlarmIdentificationNumber();
        alarmIdNum.setTerminalModelId("MODEL12");
        alarmIdNum.setTime("260623123000");
        alarmIdNum.setIndex(1);
        alarmIdNum.setAttachmentCount(2);
        alarmIdNum.setReserved(0);
        adas.setAlarmIdentificationNumber(alarmIdNum);

        item2.setAdasAlarmInfo(adas);
        items.add(item2);

        originalDto.setAdditionalItems(items);

        LocationInformationReportDto decodedDto = executeGenericTest(0x0200, "34567890123456789", 1122, originalDto, LocationInformationReportDto.class);

        assertEquals(originalDto.getAlarmFlag(), decodedDto.getAlarmFlag());
        assertEquals(originalDto.getStatus(), decodedDto.getStatus());
        assertEquals(originalDto.getLatitude(), decodedDto.getLatitude());
        assertEquals(originalDto.getLongitude(), decodedDto.getLongitude());
        assertEquals(originalDto.getElevation(), decodedDto.getElevation());
        assertEquals(originalDto.getSpeed(), decodedDto.getSpeed());
        assertEquals(originalDto.getDirection(), decodedDto.getDirection());
        assertEquals(originalDto.getTime(), decodedDto.getTime());

        assertEquals(originalDto.getAdditionalItems().size(), decodedDto.getAdditionalItems().size());

        LocationInformationReportDto.AdditionalItem decodedItem1 = decodedDto.getAdditionalItems().get(0);
        assertEquals(0x01, decodedItem1.getId());
        assertEquals(4, decodedItem1.getLength());
        assertArrayEquals(new byte[]{0x00, 0x00, 0x01, 0x2C}, decodedItem1.getRawValue());

        LocationInformationReportDto.AdditionalItem decodedItem2 = decodedDto.getAdditionalItems().get(1);
        assertEquals(0x64, decodedItem2.getId());
        assertNotNull(decodedItem2.getAdasAlarmInfo());

        LocationInformationReportDto.AdasAlarmInfo decodedAdas = decodedItem2.getAdasAlarmInfo();
        assertEquals(adas.getAlarmId(), decodedAdas.getAlarmId());
        assertEquals(adas.getPrecedingVehicleSpeed(), decodedAdas.getPrecedingVehicleSpeed());
        assertEquals(adas.getDateTime(), decodedAdas.getDateTime());

        assertNotNull(decodedAdas.getAlarmIdentificationNumber());
        assertEquals(alarmIdNum.getTerminalModelId(), decodedAdas.getAlarmIdentificationNumber().getTerminalModelId());
        assertEquals(alarmIdNum.getTime(), decodedAdas.getAlarmIdentificationNumber().getTime());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x9208)")
    void testAlarmAttachmentsUploadCommandEncodeDecode() throws Exception {
        AlarmAttachmentsUploadCommandDto originalDto = new AlarmAttachmentsUploadCommandDto();

        String ipAddress = "192.168.100.25";
        originalDto.setServerIpLength(ipAddress.length());
        originalDto.setServerIp(ipAddress);
        originalDto.setTcpPort(8080);
        originalDto.setUdpPort(8081);

        AlarmAttachmentsUploadCommandDto.AlarmIdentificationNumber alarmId = new AlarmAttachmentsUploadCommandDto.AlarmIdentificationNumber();
        alarmId.setTerminalModelId("TERM123");
        alarmId.setTime("260623143000");
        alarmId.setIndex(5);
        alarmId.setAttachmentCount(3);
        alarmId.setReserved(0);
        originalDto.setAlarmIdentificationNumber(alarmId);

        originalDto.setAlarmNo("ALARM-9999-ABCD-1234-XYZ");

        byte[] reserved = new byte[16];
        for (int i = 0; i < 16; i++) {
            reserved[i] = (byte) 0xFF;
        }
        originalDto.setReserved(reserved);

        AlarmAttachmentsUploadCommandDto decodedDto = executeGenericTest(0x9208, "34567890123456789", 4321, originalDto, AlarmAttachmentsUploadCommandDto.class);

        assertEquals(originalDto.getServerIpLength(), decodedDto.getServerIpLength());
        assertEquals(originalDto.getServerIp(), decodedDto.getServerIp());
        assertEquals(originalDto.getTcpPort(), decodedDto.getTcpPort());
        assertEquals(originalDto.getUdpPort(), decodedDto.getUdpPort());

        assertNotNull(decodedDto.getAlarmIdentificationNumber());
        assertEquals(originalDto.getAlarmIdentificationNumber().getTerminalModelId(), decodedDto.getAlarmIdentificationNumber().getTerminalModelId());
        assertEquals(originalDto.getAlarmIdentificationNumber().getTime(), decodedDto.getAlarmIdentificationNumber().getTime());
        assertEquals(originalDto.getAlarmIdentificationNumber().getIndex(), decodedDto.getAlarmIdentificationNumber().getIndex());
        assertEquals(originalDto.getAlarmIdentificationNumber().getAttachmentCount(), decodedDto.getAlarmIdentificationNumber().getAttachmentCount());

        assertEquals(originalDto.getAlarmNo(), decodedDto.getAlarmNo());
        assertArrayEquals(originalDto.getReserved(), decodedDto.getReserved());
    }


    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x1210)")
    void testAlarmAttachmentsInformationMessageEncodeDecode() throws Exception {
        AlarmAttachmentsInformationMessageDto originalDto = new AlarmAttachmentsInformationMessageDto();

        originalDto.setTerminalModelId("TERM123");

        AlarmAttachmentsInformationMessageDto.AlarmIdentificationNumber alarmId = new AlarmAttachmentsInformationMessageDto.AlarmIdentificationNumber();
        alarmId.setTerminalModelId("TERM123");
        alarmId.setTime("260623150000");
        alarmId.setIndex(1);
        alarmId.setAttachmentCount(2);
        alarmId.setReserved(0);
        originalDto.setAlarmIdentificationNumber(alarmId);

        originalDto.setAlarmNo("ALARM-8888-ZZZZ-9999-ABC");
        originalDto.setInformationType(0x01);

        List<AlarmAttachmentsInformationMessageDto.AttachmentInformation> attachments = new java.util.ArrayList<>();

        AlarmAttachmentsInformationMessageDto.AttachmentInformation att1 = new AlarmAttachmentsInformationMessageDto.AttachmentInformation();
        String fName1 = "photo_front.jpg";
        att1.setFileNameLength(fName1.length());
        att1.setFileName(fName1);
        att1.setFileSize(256000L);
        attachments.add(att1);

        AlarmAttachmentsInformationMessageDto.AttachmentInformation att2 = new AlarmAttachmentsInformationMessageDto.AttachmentInformation();
        String fName2 = "video_event.mp4";
        att2.setFileNameLength(fName2.length());
        att2.setFileName(fName2);
        att2.setFileSize(10485760L);
        attachments.add(att2);

        originalDto.setAttachmentCount(attachments.size());
        originalDto.setAttachmentInformationList(attachments);

        AlarmAttachmentsInformationMessageDto decodedDto = executeGenericTest(0x1210, "34567890123456789", 5566, originalDto, AlarmAttachmentsInformationMessageDto.class);

        assertEquals(originalDto.getTerminalModelId(), decodedDto.getTerminalModelId());

        assertNotNull(decodedDto.getAlarmIdentificationNumber());
        assertEquals(originalDto.getAlarmIdentificationNumber().getTerminalModelId(), decodedDto.getAlarmIdentificationNumber().getTerminalModelId());
        assertEquals(originalDto.getAlarmIdentificationNumber().getTime(), decodedDto.getAlarmIdentificationNumber().getTime());
        assertEquals(originalDto.getAlarmIdentificationNumber().getIndex(), decodedDto.getAlarmIdentificationNumber().getIndex());
        assertEquals(originalDto.getAlarmIdentificationNumber().getAttachmentCount(), decodedDto.getAlarmIdentificationNumber().getAttachmentCount());

        assertEquals(originalDto.getAlarmNo(), decodedDto.getAlarmNo());
        assertEquals(originalDto.getInformationType(), decodedDto.getInformationType());
        assertEquals(originalDto.getAttachmentCount(), decodedDto.getAttachmentCount());

        assertEquals(originalDto.getAttachmentInformationList().size(), decodedDto.getAttachmentInformationList().size());

        for (int i = 0; i < originalDto.getAttachmentInformationList().size(); i++) {
            AlarmAttachmentsInformationMessageDto.AttachmentInformation origAtt = originalDto.getAttachmentInformationList().get(i);
            AlarmAttachmentsInformationMessageDto.AttachmentInformation decAtt = decodedDto.getAttachmentInformationList().get(i);

            assertEquals(origAtt.getFileNameLength(), decAtt.getFileNameLength());
            assertEquals(origAtt.getFileName(), decAtt.getFileName());
            assertEquals(origAtt.getFileSize(), decAtt.getFileSize());
        }
    }

    @Test
    @DisplayName("Test Raw Stream Encode & Decode - 100KB File Segmentation (64KB chunks)")
    void testFileDataUploadStreamSegmentation() throws Exception {

        FileDataUploadCodec codec = new FileDataUploadCodec();

        int totalFileSize = 102400; // 100KB
        int maxChunkSize = 65536;   // 64KB (Batas default per tabel 64)

        byte[] dummyFile = new byte[totalFileSize];
        for (int i = 0; i < totalFileSize; i++) {
            dummyFile[i] = (byte) (i % 256);
        }

        // =======================================================
        // PENGIRIMAN FRAME 1 (Offset 0, Length 64KB)
        // =======================================================
        FileDataUploadDto frame1Dto = new FileDataUploadDto();
        frame1Dto.setFrameHeaderIdentifier(FileDataUploadCodec.FRAME_HEADER_MAGIC);
        frame1Dto.setFileName("evidence_video_100kb.mp4");
        frame1Dto.setDataOffset(0L);
        frame1Dto.setDataLength(maxChunkSize);

        byte[] chunk1 = new byte[maxChunkSize];
        System.arraycopy(dummyFile, 0, chunk1, 0, maxChunkSize);
        frame1Dto.setDataBody(chunk1);

        byte[] rawStream1 = codec.encodeStream(frame1Dto);
        FileDataUploadDto decodedFrame1 = codec.decodeStream(rawStream1);

        assertEquals(0L, decodedFrame1.getDataOffset());
        assertEquals(maxChunkSize, decodedFrame1.getDataLength());
        assertArrayEquals(chunk1, decodedFrame1.getDataBody());

        // =======================================================
        // PENGIRIMAN FRAME 2 (Offset 64KB, Length ~36KB)
        // =======================================================
        int remainingBytes = totalFileSize - maxChunkSize;

        FileDataUploadDto frame2Dto = new FileDataUploadDto();
        frame2Dto.setFrameHeaderIdentifier(FileDataUploadCodec.FRAME_HEADER_MAGIC);
        frame2Dto.setFileName("evidence_video_100kb.mp4");
        frame2Dto.setDataOffset((long) maxChunkSize);
        frame2Dto.setDataLength(remainingBytes);

        byte[] chunk2 = new byte[remainingBytes];
        System.arraycopy(dummyFile, maxChunkSize, chunk2, 0, remainingBytes);
        frame2Dto.setDataBody(chunk2);

        byte[] rawStream2 = codec.encodeStream(frame2Dto);
        FileDataUploadDto decodedFrame2 = codec.decodeStream(rawStream2);

        assertEquals((long) maxChunkSize, decodedFrame2.getDataOffset());
        assertEquals(remainingBytes, decodedFrame2.getDataLength());
        assertArrayEquals(chunk2, decodedFrame2.getDataBody());

        // =======================================================
        // INTEGRITAS FILE (Menggabungkan kembali di sisi Server)
        // =======================================================
        byte[] reconstructedFile = new byte[totalFileSize];
        System.arraycopy(decodedFrame1.getDataBody(), 0, reconstructedFile, (int) decodedFrame1.getDataOffset(), (int) decodedFrame1.getDataLength());
        System.arraycopy(decodedFrame2.getDataBody(), 0, reconstructedFile, (int) decodedFrame2.getDataOffset(), (int) decodedFrame2.getDataLength());

        assertArrayEquals(dummyFile, reconstructedFile);
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x1212)")
    void testFileUploadCompletedMessageEncodeDecode() throws Exception {
        FileUploadCompletedMessageDto originalDto = new FileUploadCompletedMessageDto();

        String fileName = "completed_video_evidence.mp4";
        originalDto.setFileNameLength(fileName.length());
        originalDto.setFileName(fileName);
        originalDto.setFileType(0x02);
        originalDto.setFileSize(15728640L);

        FileUploadCompletedMessageDto decodedDto = executeGenericTest(0x1212, "34567890123456789", 7788, originalDto, FileUploadCompletedMessageDto.class);

        assertEquals(originalDto.getFileNameLength(), decodedDto.getFileNameLength());
        assertEquals(originalDto.getFileName(), decodedDto.getFileName());
        assertEquals(originalDto.getFileType(), decodedDto.getFileType());
        assertEquals(originalDto.getFileSize(), decodedDto.getFileSize());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x1211)")
    void testFileInformationUploadEncodeDecode() throws Exception {
        FileInformationUploadDto originalDto = new FileInformationUploadDto();

        String fileName = "evidence_video_001.mp4";
        originalDto.setFileNameLength(fileName.length());
        originalDto.setFileName(fileName);
        originalDto.setFileType(0x02);
        originalDto.setFileSize(2048000L);

        FileInformationUploadDto decodedDto = executeGenericTest(0x1211, "34567890123456789", 6677, originalDto, FileInformationUploadDto.class);

        assertEquals(originalDto.getFileNameLength(), decodedDto.getFileNameLength());
        assertEquals(originalDto.getFileName(), decodedDto.getFileName());
        assertEquals(originalDto.getFileType(), decodedDto.getFileType());
        assertEquals(originalDto.getFileSize(), decodedDto.getFileSize());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x9212)")
    void testFileUploadCompletedMessageResponseSegmented() throws Exception {
        FileUploadCompletedMessageResponseDto originalDto = new FileUploadCompletedMessageResponseDto();

        String fileName = "massive_video_evidence.mp4";
        originalDto.setFileNameLength(fileName.length());
        originalDto.setFileName(fileName);
        originalDto.setFileType(0x02);
        originalDto.setUploadResult(0x01);

        List<FileUploadCompletedMessageResponseDto.RetransmissionPacket> packets = new java.util.ArrayList<>();

        for (int i = 0; i < 200; i++) {
            FileUploadCompletedMessageResponseDto.RetransmissionPacket p = new FileUploadCompletedMessageResponseDto.RetransmissionPacket();
            p.setDataOffset(i * 1024L);
            p.setDataLength(1024L);
            packets.add(p);
        }

        originalDto.setRetransmissionPacketCount(packets.size());
        originalDto.setRetransmissionPackets(packets);

        FileUploadCompletedMessageResponseDto decodedDto = executeGenericTest(0x9212, "34567890123456789", 9999, originalDto, FileUploadCompletedMessageResponseDto.class);

        assertEquals(originalDto.getFileName(), decodedDto.getFileName());
        assertEquals(200, decodedDto.getRetransmissionPackets().size());
        assertEquals(originalDto.getRetransmissionPacketCount(), decodedDto.getRetransmissionPacketCount());

        for (int i = 0; i < 200; i++) {
            assertEquals(originalDto.getRetransmissionPackets().get(i).getDataOffset(), decodedDto.getRetransmissionPackets().get(i).getDataOffset());
            assertEquals(originalDto.getRetransmissionPackets().get(i).getDataLength(), decodedDto.getRetransmissionPackets().get(i).getDataLength());
        }
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x9101)")
    void testRealTimeAudioVideoTransmissionRequestEncodeDecode() throws Exception {
        RealTimeAudioAndVideoTransmissionRequestDto originalDto = new RealTimeAudioAndVideoTransmissionRequestDto();

        String ipAddress = "192.168.1.100";
        originalDto.setServerIpLength(ipAddress.length());
        originalDto.setServerIp(ipAddress);
        originalDto.setTcpPort(9000);
        originalDto.setUdpPort(9001);
        originalDto.setLogicalChannelNumber(1);
        originalDto.setDataType(0);
        originalDto.setStreamType(0);

        RealTimeAudioAndVideoTransmissionRequestDto decodedDto = executeGenericTest(0x9101, "34567890123456789", 1234, originalDto, RealTimeAudioAndVideoTransmissionRequestDto.class);

        assertEquals(originalDto.getServerIpLength(), decodedDto.getServerIpLength());
        assertEquals(originalDto.getServerIp(), decodedDto.getServerIp());
        assertEquals(originalDto.getTcpPort(), decodedDto.getTcpPort());
        assertEquals(originalDto.getUdpPort(), decodedDto.getUdpPort());
        assertEquals(originalDto.getLogicalChannelNumber(), decodedDto.getLogicalChannelNumber());
        assertEquals(originalDto.getDataType(), decodedDto.getDataType());
        assertEquals(originalDto.getStreamType(), decodedDto.getStreamType());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x9102)")
    void testRealTimeAudioVideoTransmissionControlEncodeDecode() throws Exception {
        RealTimeAudioAndVideoTransmissionControlDto originalDto = new RealTimeAudioAndVideoTransmissionControlDto();

        originalDto.setLogicalChannelNumber(1);
        originalDto.setControlCommand(1);
        originalDto.setTurnOffAudioAndVideoType(0);
        originalDto.setSwitchStreamType(0);

        RealTimeAudioAndVideoTransmissionControlDto decodedDto = executeGenericTest(0x9102, "34567890123456789", 1234, originalDto, RealTimeAudioAndVideoTransmissionControlDto.class);

        assertEquals(originalDto.getLogicalChannelNumber(), decodedDto.getLogicalChannelNumber());
        assertEquals(originalDto.getControlCommand(), decodedDto.getControlCommand());
        assertEquals(originalDto.getTurnOffAudioAndVideoType(), decodedDto.getTurnOffAudioAndVideoType());
        assertEquals(originalDto.getSwitchStreamType(), decodedDto.getSwitchStreamType());
    }

    @Test
    @DisplayName("Test Fragmentasi Data 2000 Byte (Sesuai Batas 950 byte)")
    void testRealTimeAVStreamFragmentation() throws Exception {
        RealTimeAudioAndVideoStreamCodec codec = new RealTimeAudioAndVideoStreamCodec();
        byte[] fullData = new byte[2000]; // Data 2000 byte
        for(int i=0; i<2000; i++) fullData[i] = (byte)(i % 128);

        // Paket 1: 950 byte (First Packet: 0x0001)
        byte[] p1Data = new byte[950];
        System.arraycopy(fullData, 0, p1Data, 0, 950);
        RealTimeAudioAndVideoStreamDto dto1 = createBaseDto(123, 1, 0, 1);
        dto1.setDataBody(p1Data);
        dto1.setDataBodyLength(950);

        // Paket 2: 950 byte (Intermediate Packet: 0x0011)
        byte[] p2Data = new byte[950];
        System.arraycopy(fullData, 950, p2Data, 0, 950);
        RealTimeAudioAndVideoStreamDto dto2 = createBaseDto(124, 1, 0, 3);
        dto2.setDataBody(p2Data);
        dto2.setDataBodyLength(950);

        // Paket 3: 100 byte (Last Packet: 0x0010)
        byte[] p3Data = new byte[100];
        System.arraycopy(fullData, 1900, p3Data, 0, 100);
        RealTimeAudioAndVideoStreamDto dto3 = createBaseDto(125, 1, 0, 2);
        dto3.setDataBody(p3Data);
        dto3.setDataBodyLength(100);

        // Encode dan verifikasi
        assertNotNull(codec.encodeStream(dto1));
        assertNotNull(codec.encodeStream(dto2));
        assertNotNull(codec.encodeStream(dto3));

        assertEquals(1, dto1.getFragmentationFlag()); // First
        assertEquals(3, dto2.getFragmentationFlag()); // Intermediate
        assertEquals(2, dto3.getFragmentationFlag()); // Last
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x9205)")
    void testQueryResourceListEncodeDecode() throws Exception {
        QueryResourceListDto originalDto = new QueryResourceListDto();

        originalDto.setLogicalChannelNumber(1);
        originalDto.setStartTime("260623080000");
        originalDto.setEndedTime("260623180000");
        originalDto.setAlarmFlag(0x0000000000000001L);
        originalDto.setAudioAndVideoResourceType(0);
        originalDto.setStreamType(1);
        originalDto.setStorageDeviceType(0);

        QueryResourceListDto decodedDto = executeGenericTest(0x9205, "34567890123456789", 1234, originalDto, QueryResourceListDto.class);

        assertEquals(originalDto.getLogicalChannelNumber(), decodedDto.getLogicalChannelNumber());
        assertEquals(originalDto.getStartTime(), decodedDto.getStartTime());
        assertEquals(originalDto.getEndedTime(), decodedDto.getEndedTime());
        assertEquals(originalDto.getAlarmFlag(), decodedDto.getAlarmFlag());
        assertEquals(originalDto.getAudioAndVideoResourceType(), decodedDto.getAudioAndVideoResourceType());
        assertEquals(originalDto.getStreamType(), decodedDto.getStreamType());
        assertEquals(originalDto.getStorageDeviceType(), decodedDto.getStorageDeviceType());
    }

    @Test
    @DisplayName("Test Segmentasi Otomatis 500 Resource List (0x1205)")
    void testTrackerUploadAudioVideoResourceListSegmented() throws Exception {
        TrackerUploadOfAudioAndVideoResourceListDto originalDto = new TrackerUploadOfAudioAndVideoResourceListDto();
        originalDto.setResponseSerialNumber(1234);

        List<TrackerUploadOfAudioAndVideoResourceListDto.ResourceItem> items = new ArrayList<>();
        for(int i=0; i<500; i++) {
            TrackerUploadOfAudioAndVideoResourceListDto.ResourceItem item = new TrackerUploadOfAudioAndVideoResourceListDto.ResourceItem();
            item.setLogicalChannelNumber(i%255);
            item.setStartTime("260612010101");
            item.setEndedTime("260612020101");
            item.setAlarmFlag(0x0000000000000001L);
            item.setAudioAndVideoResourceType(i%2);
            item.setStreamType(1);
            item.setStorageDeviceType(1);
            item.setFileSize(i);
            items.add(item);
        }
        originalDto.setTotalResources(500);
        originalDto.setResourceList(items);

        TrackerUploadOfAudioAndVideoResourceListDto decodedDto = executeGenericTest(0x1205, "34567890123456789", 1234, originalDto, TrackerUploadOfAudioAndVideoResourceListDto.class);

        // 1. Assert ukuran list
        assertNotNull(decodedDto, "Decoded DTO tidak boleh null");
        assertEquals(500, decodedDto.getResourceList().size(), "Jumlah total resource harus tepat 500");
        assertEquals(originalDto.getResponseSerialNumber(), decodedDto.getResponseSerialNumber(), "Serial number harus cocok");

        // 2. Assert integritas data di posisi awal, tengah, dan akhir
        int[] indicesToVerify = {0, 250, 499};
        for (int index : indicesToVerify) {
            TrackerUploadOfAudioAndVideoResourceListDto.ResourceItem orig = originalDto.getResourceList().get(index);
            TrackerUploadOfAudioAndVideoResourceListDto.ResourceItem dec = decodedDto.getResourceList().get(index);

            assertEquals(orig.getLogicalChannelNumber(), dec.getLogicalChannelNumber(), "LogicalChannel mismatch pada index " + index);
            assertEquals(orig.getStartTime(), dec.getStartTime(), "StartTime mismatch pada index " + index);
            assertEquals(orig.getFileSize(), dec.getFileSize(), "FileSize mismatch pada index " + index);
            assertEquals(orig.getAudioAndVideoResourceType(), dec.getAudioAndVideoResourceType(), "ResourceType mismatch pada index " + index);
        }

        // 3. Assert konsistensi seluruh list
        for (int i = 0; i < 500; i++) {
            assertEquals(originalDto.getResourceList().get(i).getFileSize(),
                    decodedDto.getResourceList().get(i).getFileSize(),
                    "Integritas data rusak pada iterasi ke-" + i);
        }
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x9201)")
    void testPlatformIssuedRemoteRecordingPlaybackRequestEncodeDecode() throws Exception {
        PlatformIssuedRemoteRecordingPlaybackRequestDto originalDto = new PlatformIssuedRemoteRecordingPlaybackRequestDto();

        String ipAddress = "10.20.30.40";
        originalDto.setServerIpLength(ipAddress.length());
        originalDto.setServerIp(ipAddress);
        originalDto.setTcpPort(5050);
        originalDto.setUdpPort(5051);
        originalDto.setLogicalChannelNumber(1);
        originalDto.setAudioAndVideoType(0);
        originalDto.setStreamType(1);
        originalDto.setStorageDeviceType(1);
        originalDto.setPlaybackMode(1);
        originalDto.setFastForwardOrRewindMultiplier(2);
        originalDto.setStartTime("260623080000");
        originalDto.setEndedTime("260623100000");

        PlatformIssuedRemoteRecordingPlaybackRequestDto decodedDto = executeGenericTest(0x9201, "34567890123456789", 5432, originalDto, PlatformIssuedRemoteRecordingPlaybackRequestDto.class);

        assertEquals(originalDto.getServerIpLength(), decodedDto.getServerIpLength());
        assertEquals(originalDto.getServerIp(), decodedDto.getServerIp());
        assertEquals(originalDto.getTcpPort(), decodedDto.getTcpPort());
        assertEquals(originalDto.getUdpPort(), decodedDto.getUdpPort());
        assertEquals(originalDto.getLogicalChannelNumber(), decodedDto.getLogicalChannelNumber());
        assertEquals(originalDto.getAudioAndVideoType(), decodedDto.getAudioAndVideoType());
        assertEquals(originalDto.getStreamType(), decodedDto.getStreamType());
        assertEquals(originalDto.getStorageDeviceType(), decodedDto.getStorageDeviceType());
        assertEquals(originalDto.getPlaybackMode(), decodedDto.getPlaybackMode());
        assertEquals(originalDto.getFastForwardOrRewindMultiplier(), decodedDto.getFastForwardOrRewindMultiplier());
        assertEquals(originalDto.getStartTime(), decodedDto.getStartTime());
        assertEquals(originalDto.getEndedTime(), decodedDto.getEndedTime());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x9202)")
    void testRemoteAudioVideoRecordingPlaybackControlEncodeDecode() throws Exception {
        RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto originalDto = new RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto();

        originalDto.setAudioAndVideoChannelNumber(1);
        originalDto.setPlaybackControl(5);
        originalDto.setFastForwardOrRewindMultiplier(0);
        originalDto.setDragPlaybackPosition("260623143000");

        RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto decodedDto = executeGenericTest(0x9202, "34567890123456789", 7766, originalDto, RemoteAudioAndVideoRecordingPlaybackControlCommandIssuedByPlatformDto.class);

        assertEquals(originalDto.getAudioAndVideoChannelNumber(), decodedDto.getAudioAndVideoChannelNumber());
        assertEquals(originalDto.getPlaybackControl(), decodedDto.getPlaybackControl());
        assertEquals(originalDto.getFastForwardOrRewindMultiplier(), decodedDto.getFastForwardOrRewindMultiplier());
        assertEquals(originalDto.getDragPlaybackPosition(), decodedDto.getDragPlaybackPosition());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x9206)")
    void testFileUploadCommandEncodeDecode() throws Exception {
        FileUploadCommandDto originalDto = new FileUploadCommandDto();

        String serverAddress = "ftp.secure-server.com";
        originalDto.setServerAddressLength(serverAddress.length());
        originalDto.setServerAddress(serverAddress);

        originalDto.setPort(2121);

        String userName = "tracker_admin";
        originalDto.setUserNameLength(userName.length());
        originalDto.setUserName(userName);

        String password = "super_secret_password";
        originalDto.setPasswordLength(password.length());
        originalDto.setPassword(password);

        String uploadPath = "/uploads/2026/06/23/";
        originalDto.setFileUploadPathLength(uploadPath.length());
        originalDto.setFileUploadPath(uploadPath);

        originalDto.setLogicalChannelNumber(2);
        originalDto.setStartTime("260623100000");
        originalDto.setEndedTime("260623120000");
        originalDto.setAlarmFlag(0x0000000000000003L);
        originalDto.setAudioAndVideoResourceType(0);
        originalDto.setStreamType(1);
        originalDto.setStorageLocation(1);
        originalDto.setTaskExecutionConditions(7);

        FileUploadCommandDto decodedDto = executeGenericTest(0x9206, "34567890123456789", 8899, originalDto, FileUploadCommandDto.class);

        assertEquals(originalDto.getServerAddressLength(), decodedDto.getServerAddressLength());
        assertEquals(originalDto.getServerAddress(), decodedDto.getServerAddress());
        assertEquals(originalDto.getPort(), decodedDto.getPort());
        assertEquals(originalDto.getUserNameLength(), decodedDto.getUserNameLength());
        assertEquals(originalDto.getUserName(), decodedDto.getUserName());
        assertEquals(originalDto.getPasswordLength(), decodedDto.getPasswordLength());
        assertEquals(originalDto.getPassword(), decodedDto.getPassword());
        assertEquals(originalDto.getFileUploadPathLength(), decodedDto.getFileUploadPathLength());
        assertEquals(originalDto.getFileUploadPath(), decodedDto.getFileUploadPath());
        assertEquals(originalDto.getLogicalChannelNumber(), decodedDto.getLogicalChannelNumber());
        assertEquals(originalDto.getStartTime(), decodedDto.getStartTime());
        assertEquals(originalDto.getEndedTime(), decodedDto.getEndedTime());
        assertEquals(originalDto.getAlarmFlag(), decodedDto.getAlarmFlag());
        assertEquals(originalDto.getAudioAndVideoResourceType(), decodedDto.getAudioAndVideoResourceType());
        assertEquals(originalDto.getStreamType(), decodedDto.getStreamType());
        assertEquals(originalDto.getStorageLocation(), decodedDto.getStorageLocation());
        assertEquals(originalDto.getTaskExecutionConditions(), decodedDto.getTaskExecutionConditions());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x1206)")
    void testFileUploadCompletedNoticeEncodeDecode() throws Exception {
        FileUploadCompletedNoticeDto originalDto = new FileUploadCompletedNoticeDto();

        originalDto.setResponseSerialNumber(4321);
        originalDto.setResult(0);

        FileUploadCompletedNoticeDto decodedDto = executeGenericTest(0x1206, "34567890123456789", 1122, originalDto, FileUploadCompletedNoticeDto.class);

        assertEquals(originalDto.getResponseSerialNumber(), decodedDto.getResponseSerialNumber());
        assertEquals(originalDto.getResult(), decodedDto.getResult());
    }

    @Test
    @DisplayName("Test Encode & Decode via Message ID Routing (0x9207)")
    void testFileUploadControlEncodeDecode() throws Exception {
        FileUploadControlDto originalDto = new FileUploadControlDto();
        originalDto.setResponseSerialNumber(0x1234);
        originalDto.setUploadControl(1);

        FileUploadControlDto decodedDto = executeGenericTest(0x9207, "34567890123456789", 5556, originalDto, FileUploadControlDto.class);

        assertEquals(originalDto.getResponseSerialNumber(), decodedDto.getResponseSerialNumber());
        assertEquals(originalDto.getUploadControl(), decodedDto.getUploadControl());
    }

    private RealTimeAudioAndVideoStreamDto createBaseDto(int seq, int ch, int type, int frag) {
        RealTimeAudioAndVideoStreamDto dto = new RealTimeAudioAndVideoStreamDto();
        dto.setFrameHeaderIdentifier(0x30316364L);
        dto.setPacketSequenceNumber(seq);
        dto.setSimCardNumber("123456789012");
        dto.setLogicalChannelNumber(ch);
        dto.setDataType(type);
        dto.setFragmentationFlag(frag);
        return dto;
    }
}