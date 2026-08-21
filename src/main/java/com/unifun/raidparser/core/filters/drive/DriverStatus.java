package com.unifun.raidparser.core.filters.drive;

import com.unifun.raidparser.core.component.Severity;
import com.unifun.raidparser.core.filters.Status;
import lombok.Getter;

@Getter
public enum DriverStatus implements Status {
    INTERIM_RECOVERY_MODE(0, "Interim Recovery Mode", Severity.CRITICAL),
    DEGRADED(0, "Degraded", Severity.CRITICAL),
    FAILED(1, "OK(Failed)", Severity.CRITICAL),
    PREDICTIVE_FAILURE(2, "OK(Predictive Failure)", Severity.WARNING),
    EMPTY(3, "Empty", Severity.NO_DATA),
    OK(4, "OK", Severity.OK),
    UNSUPPORTED_TYPE(Integer.MAX_VALUE, "UNSUPPORTED_TYPE", Severity.NO_DATA),
    UNKNOWN(Integer.MAX_VALUE, "UNKNOWN", Severity.NO_DATA);

    private final int priority;
    private final String name;
    private final Severity severity;

    DriverStatus(int priority, String name, Severity severity) {
        this.priority = priority;
        this.name = name;
        this.severity = severity;
    }

    @Override
    public String toString() {
        return name;
    }
}
