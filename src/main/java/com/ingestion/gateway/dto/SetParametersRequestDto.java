package com.ingestion.gateway.dto;

public class SetParametersRequestDto {
    private Long reportingInterval; // 0x0029
    private Long turningAngle;      // 0x0030
    private Long maxSpeed;          // 0x0055
    private Long overspeedDuration; // 0x0056

    public Long getReportingInterval() { return reportingInterval; }
    public void setReportingInterval(Long reportingInterval) { this.reportingInterval = reportingInterval; }

    public Long getTurningAngle() { return turningAngle; }
    public void setTurningAngle(Long turningAngle) { this.turningAngle = turningAngle; }

    public Long getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Long maxSpeed) { this.maxSpeed = maxSpeed; }

    public Long getOverspeedDuration() { return overspeedDuration; }
    public void setOverspeedDuration(Long overspeedDuration) { this.overspeedDuration = overspeedDuration; }
}