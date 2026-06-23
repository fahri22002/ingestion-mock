package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Ad6Codec extends AdBaseCodec {
    @Override
    public int getSupportedParameterId() {
        return 0x41;
    }

    @Override
    public String getParameterName() {
        return "AD6";
    }
    @Override
    protected int getAdIndex() {
        return 6;
    }
}