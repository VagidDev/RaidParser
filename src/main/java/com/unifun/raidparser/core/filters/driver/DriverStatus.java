package com.unifun.raidparser.core.filters.driver;

import com.unifun.raidparser.core.filters.Status;

public enum DriverStatus implements Status {
    INTERIM_RECOVERY_MODE(0, "Interim Recovery Mode"),
    PREDICTIVE_FAILURE(1, "OK(Predictive Failure)"),
    FAILED(2, "OK(Failed)"),
    UNKNOW(3, "UNKNOWN"),
    EMPTY(4, "Empty"),
    OK(Integer.MAX_VALUE, "OK");

    private final int priority;
    private final String name;

    DriverStatus(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
