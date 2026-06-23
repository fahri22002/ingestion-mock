// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.IBeaconGroupACodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IBeaconGroupBCodec extends IBeaconGroupBaseCodec {

    @Override
    public int getSupportedParameterId() {
        return 0xFE71;
    }

    @Override
    public String getParameterName() {
        return "iBeacon Group A";
    }
}