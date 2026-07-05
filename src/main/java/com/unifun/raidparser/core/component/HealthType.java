package com.unifun.raidparser.core.component;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum HealthType {
    @JsonProperty("drive_health")
    DRIVE_HEALTH,
    @JsonProperty("psu_health")
    PSU_HEALTH,
    @JsonProperty("battery_health")
    BATTERY_HEALTH
}
