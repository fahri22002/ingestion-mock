package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Ad2Codec extends AdBaseCodec {
    @Override
    public int getSupportedParameterId() {
        return 0x17;
    }
    @Override
    public String getParameterName() {
        return "AD2";
    }
    @Override
    protected int getAdIndex() {
        return 2;
    }
}