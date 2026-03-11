package com.unifun.components.filters.battery;

import com.unifun.components.filters.Status;

public enum BatteryStatus implements Status {
    FAILED(0, "Failed (Replace Batteries)"),
    RECHARGING(1, "Recharging"),
    NO_BATTERY(2, "No battery"),
    NOT_SAFE(3, "OK(Not safe)"),
    CACHE_DISABLED(4, "Permanently Disabled"),
    UNKNOWN(5, "UNKNOWN"),
    EMPTY(6, "Empty"),
    OK(Integer.MAX_VALUE, "Ok");


    private final int priority;
    private final String name;

    BatteryStatus(int priority, String name) {
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
