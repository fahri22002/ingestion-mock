package com.ingestion.gateway.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@ApplicationScoped
public class DeviceConnectionManager {

    // Menyimpan pemetaan antara IMEI dan fungsi socket writer (replier)
    private final ConcurrentHashMap<String, Consumer<byte[]>> activeSessions = new ConcurrentHashMap<>();

    public void registerSession(String imei, Consumer<byte[]> replier) {
        activeSessions.put(imei, replier);
    }

    public Consumer<byte[]> getSession(String imei) {
        return activeSessions.get(imei);
    }

    public void removeSession(String imei) {
        activeSessions.remove(imei);
    }
}