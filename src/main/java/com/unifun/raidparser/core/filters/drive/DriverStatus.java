package com.unifun.raidparser.core.filters.drive;

import com.unifun.raidparser.core.filters.Status;
import lombok.Getter;

@Getter
public enum DriverStatus implements Status {
    INTERIM_RECOVERY_MODE(0, "Interim Recovery Mode"),
    DEGRADED(0, "Degraded"),
    FAILED(1, "OK(Failed)"),
    PREDICTIVE_FAILURE(2, "OK(Predictive Failure)"),
    EMPTY(3, "Empty"),
    OK(4, "OK"),
    UNSUPPORTED_TYPE(Integer.MAX_VALUE, "UNSUPPORTED_TYPE"),
    UNKNOWN(Integer.MAX_VALUE, "UNKNOWN");

    private final int priority;
    private final String name;

    DriverStatus(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
