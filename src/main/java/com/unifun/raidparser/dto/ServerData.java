package com.unifun.raidparser.dto;

import com.unifun.raidparser.core.component.HealthType;

import java.util.Map;

public record ServerData(
        String serverName,
        Map<HealthType, String> rawDataByComponent
) {
    public String getRawData(HealthType type) {
        return rawDataByComponent.get(type);
    }
}
