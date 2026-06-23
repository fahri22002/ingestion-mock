package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.QuerySpecifiedTrackerParametersDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class QuerySpecifiedTrackerParametersCodec implements Jtt808MessageCodec<QuerySpecifiedTrackerParametersDto> {

    private static final Logger LOG = Logger.getLogger(QuerySpecifiedTrackerParametersCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x8106; // ID Mesej untuk Query Specified Tracker Parameters
    }

    @Override
    public Class<QuerySpecifiedTrackerParametersDto> getSupportedDtoClass() {
        return QuerySpecifiedTrackerParametersDto.class;
    }

    @Override
    public String getCommandName() {
        return "Query Specified Tracker Parameters";
    }

    @Override
    public QuerySpecifiedTrackerParametersDto decodeBody(byte[] bodyData) {
        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyData);
        QuerySpecifiedTrackerParametersDto dto = new QuerySpecifiedTrackerParametersDto();
        List<Long> parameterIds = new ArrayList<>();

        try {
            // 1. Ambil Total Number of Parameters (BYTE - 1 byte)
            int totalParameters = Byte.toUnsignedInt(bodyBuffer.get());
            dto.setTotalParameters(totalParameters);

            // 2. Ekstrak Senarai Parameter ID (DWORD - 4 byte setiap satu) berdasarkan nilai 'n'
            for (int i = 0; i < totalParameters; i++) {
                if (bodyBuffer.hasRemaining()) {
                    long paramId = Integer.toUnsignedLong(bodyBuffer.getInt());
                    parameterIds.add(paramId);
                }
            }
            dto.setParameterIds(parameterIds);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Total Parameters: %d", totalParameters);
            LOG.infof("Parameter IDs   : %s", parameterIds.toString());

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Query Specified Tracker Parameters", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(QuerySpecifiedTrackerParametersDto dto) {
        try {
            List<Long> parameterIds = dto.getParameterIds() != null ? dto.getParameterIds() : new ArrayList<>();
            int totalParameters = parameterIds.size();

            // Alokasi Buffer: 1 byte (Total Parameter) + (4 byte x jumlah parameter)
            ByteBuffer buffer = ByteBuffer.allocate(1 + (4 * totalParameters));

            // 1. Tulis Total Number of Parameters (BYTE)
            buffer.put((byte) totalParameters);

            // 2. Tulis Parameter ID List secara sekuensial (DWORD)
            for (Long paramId : parameterIds) {
                buffer.putInt(paramId.intValue());
            }

            return buffer.array();
        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Query Specified Tracker Parameters", e);
            return new byte[0];
        }
    }
}