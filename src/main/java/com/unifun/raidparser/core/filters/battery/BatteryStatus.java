package com.unifun.raidparser.core.filters.battery;

import com.unifun.raidparser.core.filters.Status;
import lombok.Getter;

@Getter
public enum BatteryStatus implements Status {
    FAILED(0, "Failed (Replace Batteries)"),
    RECHARGING(1, "Recharging"),
    NO_BATTERY(2, "No battery"),
    NOT_SAFE(3, "OK(Not safe)"),
    CACHE_DISABLED(4, "Permanently Disabled"),
    EMPTY(5, "Empty"),
    OK(6, "Ok"),
    UNKNOWN(Integer.MAX_VALUE, "UNKNOWN");


    private final int priority;
    private final String name;

    BatteryStatus(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
