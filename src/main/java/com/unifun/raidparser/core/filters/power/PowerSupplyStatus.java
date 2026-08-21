package com.unifun.raidparser.core.filters.power;

import com.unifun.raidparser.core.component.Severity;
import com.unifun.raidparser.core.filters.Status;
import lombok.Getter;

@Getter
public enum PowerSupplyStatus implements Status {
    FAILED(0, "Failed", Severity.CRITICAL),
    NOT_PRESENT(1, "Power Supply Not Present", Severity.WARNING),
    UNCLAIMED(2, "Unclaimed", Severity.WARNING),
    EMPTY(3, "Empty", Severity.NO_DATA),
    OK(4, "OK", Severity.OK),
    UNSUPPORTED_TYPE(Integer.MAX_VALUE, "UNSUPPORTED_TYPE", Severity.NO_DATA),
    UNKNOWN(Integer.MAX_VALUE, "UNKNOWN", Severity.NO_DATA);

    private final int priority;
    private final String name;
    private final Severity severity;

    PowerSupplyStatus(int priority, String name, Severity severity) {
        this.priority = priority;
        this.name = name;
        this.severity = severity;
    }

    @Override
    public String toString() {
        return name;
    }
}
