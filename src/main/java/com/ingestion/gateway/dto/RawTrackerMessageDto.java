package com.ingestion.gateway.dto;

public class RawTrackerMessageDto {
    private String remoteAddress;
    private String protocol; // "TCP" atau "UDP"
    private String hexPayload;

    // Nanti Anda bisa menambahkan field spesifik JTT808 di sini:
    // private String messageId;
    // private String imei;

    public RawTrackerMessageDto(String remoteAddress, String protocol, String hexPayload) {
        this.remoteAddress = remoteAddress;
        this.protocol = protocol;
        this.hexPayload = hexPayload;
    }

    public String getRemoteAddress() { return remoteAddress; }
    public String getProtocol() { return protocol; }
    public String getHexPayload() { return hexPayload; }

    @Override
    public String toString() {
        return String.format("[%s] From: %s | Payload: %s", protocol, remoteAddress, hexPayload);
    }
}