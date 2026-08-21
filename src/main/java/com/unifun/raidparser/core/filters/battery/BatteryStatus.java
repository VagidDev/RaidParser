package com.unifun.raidparser.core.filters.battery;

import com.unifun.raidparser.core.component.Severity;
import com.unifun.raidparser.core.filters.Status;
import lombok.Getter;

@Getter
public enum BatteryStatus implements Status {
    FAILED(0, "Failed (Replace Batteries)", Severity.CRITICAL),
    RECHARGING(1, "Recharging", Severity.WARNING),
    NO_BATTERY(2, "No battery", Severity.WARNING),
    NOT_SAFE(3, "OK(Not safe)", Severity.WARNING),
    CACHE_DISABLED(4, "Permanently Disabled", Severity.WARNING),
    EMPTY(5, "Empty", Severity.NO_DATA),
    OK(6, "Ok", Severity.OK),
    UNSUPPORTED_TYPE(Integer.MAX_VALUE, "UNSUPPORTED_TYPE", Severity.NO_DATA),
    UNKNOWN(Integer.MAX_VALUE, "UNKNOWN", Severity.NO_DATA);

    private final int priority;
    private final String name;
    private final Severity severity;

    BatteryStatus(int priority, String name, Severity severity) {
        this.priority = priority;
        this.name = name;
        this.severity = severity;
    }

    @Override
    public String toString() {
        return name;
    }
}
