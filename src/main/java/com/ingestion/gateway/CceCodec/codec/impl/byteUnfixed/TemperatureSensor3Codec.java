// File: com.ingestion.gateway.CceCoder.codec.impl.byte3.TemperatureSensorCodec.java
package com.ingestion.gateway.CceCodec.codec.impl.byteUnfixed;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TemperatureSensor3Codec extends TemperatureSensorBaseCodec {
    @Override public int getSupportedParameterId() { return 0x2C; }
    @Override public String getParameterName() { return "Temperature Sensor 3"; }
}