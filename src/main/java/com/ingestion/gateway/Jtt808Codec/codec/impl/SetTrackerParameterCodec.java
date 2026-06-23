package com.ingestion.gateway.Jtt808Codec.codec.impl;

import com.ingestion.gateway.Jtt808Codec.codec.Jtt808MessageCodec;
import com.ingestion.gateway.Jtt808Codec.dto.impl.core.SetTrackerParameterDto.ParameterItem;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SetTrackerParameterCodec implements Jtt808MessageCodec<com.ingestion.gateway.Jtt808Codec.dto.impl.core.SetTrackerParameterDto> {

    private static final Logger LOG = Logger.getLogger(SetTrackerParameterCodec.class);

    @Override
    public int getSupportedMessageId() {
        return 0x8103; // ID Perintah untuk Set Tracker Parameter
    }

    @Override
    public Class<com.ingestion.gateway.Jtt808Codec.dto.impl.core.SetTrackerParameterDto> getSupportedDtoClass() {
        return com.ingestion.gateway.Jtt808Codec.dto.impl.core.SetTrackerParameterDto.class;
    }

    @Override
    public String getCommandName() {
        return "Set Tracker Parameter";
    }

    @Override
    public com.ingestion.gateway.Jtt808Codec.dto.impl.core.SetTrackerParameterDto decodeBody(byte[] bodyData) {
        ByteBuffer bodyBuffer = ByteBuffer.wrap(bodyData);
        com.ingestion.gateway.Jtt808Codec.dto.impl.core.SetTrackerParameterDto dto = new com.ingestion.gateway.Jtt808Codec.dto.impl.core.SetTrackerParameterDto();
        List<ParameterItem> items = new ArrayList<>();

        try {
            // 1. Ambil Total Parameter (BYTE - 1 byte)
            int totalParameters = Byte.toUnsignedInt(bodyBuffer.get());
            dto.setTotalParameters(totalParameters);

            // 2. Loop ekstraksi item parameter berdasarkan ketersediaan buffer
            while (bodyBuffer.hasRemaining()) {
                // DWORD (4 byte): Parameter ID
                long paramId = Integer.toUnsignedLong(bodyBuffer.getInt());

                // BYTE (1 byte): Parameter Length
                int paramLen = Byte.toUnsignedInt(bodyBuffer.get());

                // BYTE[n]: Parameter Value sesuai panjang paramLen
                byte[] paramValue = new byte[paramLen];
                bodyBuffer.get(paramValue);

                ParameterItem item = new ParameterItem(paramId, paramLen, paramValue);
                items.add(item);
            }

            dto.setItems(items);

            LOG.infof("=== Hasil Decode Body (0x%x - %s) ===", getSupportedMessageId(), getCommandName());
            LOG.infof("Total Params    : %d (Actual List Size: %d)", totalParameters, items.size());
            for (ParameterItem item : items) {
                LOG.infof(" -> Param ID [0x%04X] | Length: %d", item.getParameterId(), item.getParameterLength());
            }

        } catch (Exception e) {
            LOG.error("Gagal mengekstrak body Set Tracker Parameter", e);
            return null;
        }
        return dto;
    }

    @Override
    public byte[] encodeValue(com.ingestion.gateway.Jtt808Codec.dto.impl.core.SetTrackerParameterDto dto) {
        try {
            List<ParameterItem> items = dto.getItems() != null ? dto.getItems() : new ArrayList<>();

            // Hitung total alokasi ukuran buffer dinamis
            // 1 byte (Total Parameter) + sum dari setiap item [4 byte (ID) + 1 byte (Length) + n byte (Value)]
            int totalSize = 1;
            for (ParameterItem item : items) {
                totalSize += 4 + 1 + item.getParameterValue().length;
            }

            ByteBuffer buffer = ByteBuffer.allocate(totalSize);

            // 1. Tulis Total Parameter (BYTE)
            buffer.put((byte) items.size());

            // 2. Tulis Parameter Item List secara sekuensial
            for (ParameterItem item : items) {
                // DWORD (4 byte)
                buffer.putInt((int) item.getParameterId());

                // BYTE (1 byte)
                buffer.put((byte) item.getParameterValue().length);

                // Value (n byte)
                buffer.put(item.getParameterValue());
            }

            return buffer.array();

        } catch (Exception e) {
            LOG.error("Gagal melakukan encode body Set Tracker Parameter", e);
            return new byte[0];
        }
    }
}