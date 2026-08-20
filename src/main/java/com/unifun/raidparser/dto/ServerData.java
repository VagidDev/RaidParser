package com.unifun.raidparser.dto;

import com.unifun.raidparser.core.component.HealthType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ServerData(
        String serverName,
        Map<HealthType, String> rawDataByComponent
) {
    public String getRawData(HealthType type) {
        return rawDataByComponent.get(type);
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("Server -> " + serverName + "\n" + "Health -> ");
        if (rawDataByComponent != null) rawDataByComponent.forEach((k,v) -> output.append("Type: ").append(k).append(" Data: ").append(v));
        return output.toString();
    }
}
