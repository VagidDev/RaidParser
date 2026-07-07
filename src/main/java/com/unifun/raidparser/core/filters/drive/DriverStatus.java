package com.unifun.raidparser.core.filters.drive;

import com.unifun.raidparser.core.filters.Status;
import lombok.Getter;

@Getter
public enum DriverStatus implements Status {
    INTERIM_RECOVERY_MODE(0, "Interim Recovery Mode"),
    DEGRADED(1, "Degraded"),
    PREDICTIVE_FAILURE(1, "OK(Predictive Failure)"),
    FAILED(2, "OK(Failed)"),
    EMPTY(3, "Empty"),
    OK(4, "OK"),
    UNKNOW(Integer.MAX_VALUE, "UNKNOWN");

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
