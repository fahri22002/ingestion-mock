package com.ingestion.gateway.dto;
public class VideoResourceDto {
    private String imei;
    private int channelNumber;
    private long fileSize;
    private String startTime;
    private String endTime;
    // ... getter, setter, dan constructor


    public void setImei(String imei) {
        this.imei = imei;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}