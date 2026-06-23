package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.FileUploadControlDto;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.QueryTrackerParametersResponseDto;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.QueryTrackerParametersResponseDto.ParameterItem;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class QueryTrackerParametersResponseCodec implements Jtt808MessageCodec<QueryTrackerParametersResponseDto> {

    private static final Logger LOG = Logger.getLogger(QueryTrackerParametersResponseCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x0104; // ID Perintah untuk Query Tracker Parameters Response
    }

    @Override
    public Class<QueryTrackerParametersResponseDto> getSupportedDtoClass() {
        return QueryTrackerParametersResponseDto.class;
    }

    @Override
    public String getCommandName() {
        return "Query Tracker Parameters Response";
    }

    @Override
    public QueryTrackerParametersResponseDto decodeBody(byte[] bodyData) {
        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyData);
        QueryTrackerParametersResponseDto dto = new QueryTrackerParametersResponseDto();
        List<ParameterItem> items = new ArrayList<>();

        try {
            // 1. WORD (2 byte): Response Serial Number
            int responseSerialNumber = Short.toUnsignedInt(bodyBuffer.getShort());
            dto.setResponseSerialNumber(responseSerialNumber);

            // 2. BYTE (1 byte): Number of response parameters
            int totalParameters = Byte.toUnsignedInt(bodyBuffer.get());
            dto.setTotalParameters(totalParameters);

            // 3. Loop pengekstrakan item parameter [cite: 118]
            for (int i = 0; i < totalParameters; i++) {
                if (bodyBuffer.hasRemaining()) {
                    // DWORD (4 byte): Parameter ID [cite: 109]
                    long paramId = Integer.toUnsignedLong(bodyBuffer.getInt());

                    // BYTE (1 byte): Parameter Length [cite: 109]
                    int paramLen = Byte.toUnsignedInt(bodyBuffer.get());

                    // BYTE[n]: Parameter Value [cite: 109]
                    byte[] paramValue = new byte[paramLen];
                    bodyBuffer.get(paramValue);

                    ParameterItem item = new ParameterItem(paramId, paramLen, paramValue);
                    items.add(item);
                }
            }

            dto.setItems(items);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Response Ser Num: %d", responseSerialNumber);
            LOG.infof("Total Params    : %d (Actual List Size: %d)", totalParameters, items.size());
            for (ParameterItem item : items) {
                LOG.infof(" -> Param ID [0x%04X] | Length: %d", item.getParameterId(), item.getParameterLength());
            }

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Query Tracker Parameters Response", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(QueryTrackerParametersResponseDto dto) {
        try {
            List<ParameterItem> items = dto.getItems() != null ? dto.getItems() : new ArrayList<>();
            int totalParameters = items.size();

            // Pengiraan peruntukan jumlah memori buffer (2 byte + 1 byte + jumlah saiz item parameter)
            int totalSize = 3;
            for (ParameterItem item : items) {
                totalSize += 4 + 1 + item.getParameterValue().length;
            }

            ByteBuffer buffer = ByteBuffer.allocate(totalSize);

            // 1. Tulis Response Serial Number (WORD)
            buffer.putShort((short) dto.getResponseSerialNumber());

            // 2. Tulis Number of response parameters (BYTE)
            buffer.put((byte) totalParameters);

            // 3. Tulis Parameter Item List secara jujukan [cite: 118]
            for (ParameterItem item : items) {
                // DWORD (4 byte): Parameter ID [cite: 109]
                buffer.putInt((int) item.getParameterId());

                // BYTE (1 byte): Parameter Length [cite: 109]
                buffer.put((byte) item.getParameterValue().length);

                // Value (n byte): Parameter Value [cite: 109]
                buffer.put(item.getParameterValue());
            }

            return buffer.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Query Tracker Parameters Response", e);
            return new byte[0];
        }
    }

    @ApplicationScoped
    public static class FileUploadControlCodec implements Jtt808MessageCodec<FileUploadControlDto> {

        private static final Logger LOG = Logger.getLogger(FileUploadControlCodec.class);

        @Override
        public int getSupportedMessageId() {
            return 0x9207; // ID Mesej untuk File Upload Control
        }

        @Override
        public Class<FileUploadControlDto> getSupportedDtoClass() {
            return FileUploadControlDto.class;
        }

        @Override
        public String getCommandName() {
            return "File Upload Control";
        }

        @Override
        public FileUploadControlDto decodeBody(byte[] bodyData) {
            ByteBuffer buf = ByteBuffer.wrap(bodyData);
            FileUploadControlDto dto = new FileUploadControlDto();

            try {
                // 1. WORD (2 byte): Response Serial Number
                dto.setResponseSerialNumber(Short.toUnsignedInt(buf.getShort()));

                // 2. BYTE (1 byte): Upload Control
                dto.setUploadControl(Byte.toUnsignedInt(buf.get()));

                LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
                LOG.infof("Response Serial Num : %d", dto.getResponseSerialNumber());
                LOG.infof("Upload Control      : %d (0: Pause, 1: Continue, 2: Cancel)", dto.getUploadControl());

            } catch (Exception e) {
                LOG.error("Gagal mengekstrak body File Upload Control", e);
                return null;
            }
            return dto;
        }

        @Override
        public byte[] encodeValue(FileUploadControlDto dto) {
            try {
                // Total alokasi buffer konstan: 2 (WORD) + 1 (BYTE) = 3 byte
                ByteBuffer buf = ByteBuffer.allocate(3);

                // 1. WORD: Response Serial Number
                buf.putShort((short) dto.getResponseSerialNumber());

                // 2. BYTE: Upload Control
                buf.put((byte) dto.getUploadControl());

                return buf.array();

            } catch (Exception e) {
                LOG.error("Gagal melakukan encode body File Upload Control", e);
                return new byte[0];
            }
        }
    }
}