package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.QueryTrackerParametersDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class QueryTrackerParametersCodec implements Jtt808MessageCodec<QueryTrackerParametersDto> {

    private static final Logger LOG = Logger.getLogger(QueryTrackerParametersCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x8104; // ID Perintah untuk Query Tracker Parameters
    }

    @Override
    public Class<QueryTrackerParametersDto> getSupportedDtoClass() {
        return QueryTrackerParametersDto.class;
    }

    @Override
    public String getCommandName() {
        return "Query Tracker Parameters";
    }

    @Override
    public QueryTrackerParametersDto decodeBody(byte[] bodyData) {
        LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
        LOG.info("Message body untuk perintah ini kosong.");

        // Mengembalikan objek DTO kosong karena tidak ada payload data yang perlu diekstrak
        return new QueryTrackerParametersDto();
    }

    @Override
    public byte[] encodeValue(QueryTrackerParametersDto dto) {
        // Mengembalikan array byte kosong (0 byte) sesuai dengan spesifikasi dokumen
        return new byte[0];
    }
}