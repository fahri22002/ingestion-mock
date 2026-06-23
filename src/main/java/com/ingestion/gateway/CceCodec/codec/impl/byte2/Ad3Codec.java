package com.ingestion.gateway.CceCodec.codec.impl.byte2;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Ad3Codec extends AdBaseCodec {
    @Override
    public int getSupportedParameterId() {
        return 0x18;
    }

    @Override
    public String getParameterName() {
        return "AD3";
    }
    @Override
    protected int getAdIndex() {
        return 3;
    }
}