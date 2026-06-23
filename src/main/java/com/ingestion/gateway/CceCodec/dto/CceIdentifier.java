package com.ingestion.gateway.CceCodec.dto;

public enum CceIdentifier {
    PERIODIC_REPORT('P', "Periodic/Position Report"),
    ALARM_REPORT('A', "Alarm Report"),
    EVENT_REPORT('E', "Event Report"),
    RESPONSE('R', "Response from Tracker"),
    UNKNOWN('?', "Unknown Identifier");

    private final char code;
    private final String description;

    CceIdentifier(char code, String description) {
        this.code = code;
        this.description = description;
    }

    public char getCode() { return code; }
    public String getDescription() { return description; }

    public static CceIdentifier fromChar(char c) {
        for (CceIdentifier id : values()) {
            if (id.code == c) return id;
        }
        return UNKNOWN;
    }
}