package com.ingestion.gateway.CceCodec.codec;

import com.ingestion.gateway.CceCodec.dto.CceDto;
import com.ingestion.gateway.CceCodec.dto.CceMessageEnvelope;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CceEncoderDispatcher {

    private static final Logger LOG = Logger.getLogger(CceEncoderDispatcher.class);

    @Inject
    Instance<CceMessageCodec<? extends CceDto>> availableEncoders;

    private final Map<Integer, CceMessageCodec<CceDto>> encoderRegistry = new HashMap<>();

    @SuppressWarnings("unchecked")
    void onStart(@Observes StartupEvent ev) {
        for (CceMessageCodec<?> encoder : availableEncoders) {
            encoderRegistry.put(encoder.getSupportedParameterId(), (CceMessageCodec<CceDto>) encoder);
        }
    }

    public byte[] encode(CceMessageEnvelope envelope) throws Exception {

        List<EncodedParam> byte1Params = new ArrayList<>();
        List<EncodedParam> byte2Params = new ArrayList<>();
        List<EncodedParam> byte4Params = new ArrayList<>();
        List<EncodedParam> mixedParams = new ArrayList<>();

        for (CceDto dto : envelope.getParameters()) {
            int paramId = dto.getParameterId();
            CceMessageCodec<CceDto> encoder = encoderRegistry.get(paramId);

            if (encoder == null || !encoder.getSupportedDtoClass().isInstance(dto)) {
                continue;
            }

            byte[] valueBytes = encoder.encodeValue(dto);
            int category = encoder.getByteCategory();

            switch (category) {
                case 1: byte1Params.add(new EncodedParam(paramId, valueBytes, false)); break;
                case 2: byte2Params.add(new EncodedParam(paramId, valueBytes, false)); break;
                case 4: byte4Params.add(new EncodedParam(paramId, valueBytes, false)); break;
                default: mixedParams.add(new EncodedParam(paramId, valueBytes, (category == -1))); break;
            }
        }

        // Rakit 1 Data Packet utuh
        ByteArrayOutputStream paramStream = new ByteArrayOutputStream();
        writeParamGroup(paramStream, byte1Params);
        writeParamGroup(paramStream, byte2Params);
        writeParamGroup(paramStream, byte4Params);
        writeMixedParamGroup(paramStream, mixedParams);

        byte[] allParamsBytes = paramStream.toByteArray();

        // Total IDs = 2 bytes. Jadi packet length = 2 + allParamsBytes.length
        int totalIds = byte1Params.size() + byte2Params.size() + byte4Params.size() + mixedParams.size();
        int packetLength = 2 + allParamsBytes.length;

        // Rakit Body Binary (LITTLE ENDIAN)
        ByteBuffer binaryPayloadBuffer = ByteBuffer.allocate(6 + 2 + packetLength).order(ByteOrder.LITTLE_ENDIAN);
        binaryPayloadBuffer.putInt(envelope.getCacheRemaining()); // 4 byte
        binaryPayloadBuffer.putShort((short) 1); // Num Packets = 1 (Encoder hanya membuat real-time data 1 paket)
        binaryPayloadBuffer.putShort((short) packetLength); // 2 byte
        binaryPayloadBuffer.putShort((short) totalIds); // 2 byte
        binaryPayloadBuffer.put(allParamsBytes); // N byte

        byte[] binaryPayloadBytes = binaryPayloadBuffer.array();

        // Rakit Header Pembungkus CCE (Kaku sesuai standard)
        // Format: $$<Ident><DataLength>,<IMEI>,CCE,<BinaryPayload>*<CS>\r\n
        // Data length rules: panjang string dari ',' pertama sampai '\r\n' inklusif

        // 1. Hitung Panjang String / Data Length
        int lengthFromCommaToEnd = 1 + envelope.getImei().length() + 5 + binaryPayloadBytes.length + 1 + 2 + 2;
        // 1 (',') + IMEI length + 5 (",CCE,") + binaryLength + 1 ('*') + 2 (ChecksumHex) + 2 ('\r\n')

        String headerPrefix = String.format("$$%c%d", envelope.getIdentifier().getCode(), lengthFromCommaToEnd);
        String imeiCCE = "," + envelope.getImei() + ",CCE,";

        ByteArrayOutputStream finalStream = new ByteArrayOutputStream();
        finalStream.write(headerPrefix.getBytes(StandardCharsets.US_ASCII));
        finalStream.write(imeiCCE.getBytes(StandardCharsets.US_ASCII));
        finalStream.write(binaryPayloadBytes);
        finalStream.write((byte) 0x2A); // Marker '*'

        // Kalkulasi Checksum Akumulatif (Dari $$ hingga *)
        byte[] currentBytes = finalStream.toByteArray();
        int sum = 0;
        for (byte b : currentBytes) {
            sum += Byte.toUnsignedInt(b);
        }

        // Ambil low byte dan convert ke format ASCII Hex 2 digit uppercase
        String checksumHex = String.format("%02X", sum & 0xFF);

        finalStream.write(checksumHex.getBytes(StandardCharsets.US_ASCII));
        finalStream.write(new byte[]{0x0D, 0x0A}); // '\r\n'

        return finalStream.toByteArray();
    }

    // Helper method sama persis seperti sebelumnya...
    private void writeParamGroup(ByteArrayOutputStream stream, List<EncodedParam> params) throws Exception {
        stream.write((byte) params.size());
        for (EncodedParam p : params) { writeParamId(stream, p.id); stream.write(p.value); }
    }

    private void writeMixedParamGroup(ByteArrayOutputStream stream, List<EncodedParam> params) throws Exception {
        stream.write((byte) params.size());
        for (EncodedParam p : params) {
            writeParamId(stream, p.id);
            if (p.needsLengthByte) { stream.write((byte) p.value.length); }
            stream.write(p.value);
        }
    }

    private void writeParamId(ByteArrayOutputStream stream, int id) {
        if ((id >> 8) == 0xFE || (id >> 8) == 0xF8) {
            stream.write((byte) (id >> 8)); stream.write((byte) (id & 0xFF));
        } else {
            stream.write((byte) id);
        }
    }

    private static class EncodedParam {
        final int id; final byte[] value; final boolean needsLengthByte;
        EncodedParam(int id, byte[] value, boolean needsLengthByte) {
            this.id = id; this.value = value; this.needsLengthByte = needsLengthByte;
        }
    }
}