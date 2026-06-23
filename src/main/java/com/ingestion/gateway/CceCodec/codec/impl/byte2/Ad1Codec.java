package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Ad1Codec extends AdBaseCodec {
    @Override
    public int getSupportedParameterId() {
        return 0x16;
    }
    @Override
    public String getParameterName() {
        return "AD1";
    }
    @Override
    protected int getAdIndex() {
        return 1;
    }
}
