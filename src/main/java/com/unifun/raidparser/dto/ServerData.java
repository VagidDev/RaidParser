package com.unifun.raidparser.dto;

public record ServerData(
        String serverName,
        String driveHealthData,
        String psuHealthData,
        String batteryHealthData
) {}
