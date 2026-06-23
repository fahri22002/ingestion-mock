package com.ingestion.gateway.CceCodec.codec.impl.byte4;

import com.ingestion.gateway.CceCodec.codec.core.AbstractByte4Codec;
import com.ingestion.gateway.CceCodec.dto.impl.byte4.DrivingUnderTheInfluenceOfFatigueDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DrivingUnderTheInfluenceOfFatigueCodec extends AbstractByte4Codec<DrivingUnderTheInfluenceOfFatigueDto> {

    @Override
    public Class<DrivingUnderTheInfluenceOfFatigueDto> getSupportedDtoClass() {
        return DrivingUnderTheInfluenceOfFatigueDto.class;
    }

    @Override
    public int getSupportedParameterId() {
        return 0xfe2f;
    }

    @Override
    public String getParameterName() {
        return "Driving under the influence of fatigue";
    }

    @Override
    protected DrivingUnderTheInfluenceOfFatigueDto mapToDto(long decodedValue) {
        DrivingUnderTheInfluenceOfFatigueDto dto = new DrivingUnderTheInfluenceOfFatigueDto();
        dto.setDrivingUnderTheInfluenceOfFatigue(decodedValue);
        return dto;
    }

    @Override
    protected long mapFromDto(DrivingUnderTheInfluenceOfFatigueDto dto) {
        return dto.getDrivingUnderTheInfluenceOfFatigue();
    }
}