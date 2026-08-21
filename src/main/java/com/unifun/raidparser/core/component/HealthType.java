package com.unifun.raidparser.core.component;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public enum HealthType {
    @JsonProperty("drive_health")
    DRIVE_HEALTH,
    @JsonProperty("psu_health")
    PSU_HEALTH,
    @JsonProperty("battery_health")
    BATTERY_HEALTH,

    @JsonEnumDefaultValue
    UNKNOWN;

    private static final List<HealthType> REPORTABLE = List.of(DRIVE_HEALTH, PSU_HEALTH, BATTERY_HEALTH);

    /** Типы, которые попадают в отчёты и экспорт: всё, кроме UNKNOWN. */
    public static List<HealthType> reportable() {
        return REPORTABLE;
    }
}
