package com.unifun.raidparser.dto;

import com.unifun.raidparser.core.component.ComponentType;

import java.util.Map;

public record ServerData(
        String serverName,
        Map<ComponentType, String> rawDataByComponent
) {
    public String getRawData(ComponentType type) {
        return rawDataByComponent.get(type);
    }
}
