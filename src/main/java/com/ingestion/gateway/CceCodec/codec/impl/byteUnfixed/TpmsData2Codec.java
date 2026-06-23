// File: com.ingestion.gateway.CceCoder.codec.impl.byteUnfixed.TpmsData1Codec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TpmsData2Codec extends TpmsDataBaseCodec {

    @Override
    public int getSupportedParameterId() {
        return 0xFEF3;
    }

    @Override
    public String getParameterName() {
        return "TPMS data 2";
    }
}