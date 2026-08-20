package com.unifun.raidparser.core.filters.power;

import com.unifun.raidparser.core.filters.Status;
import lombok.Getter;

@Getter
public enum PowerSupplyStatus implements Status {
    FAILED(0, "Failed"),
    NOT_PRESENT(1, "Power Supply Not Present"),
    UNCLAIMED(2, "Unclaimed"),
    EMPTY(3, "Empty"),
    OK(4, "OK"),
    UNSUPPORTED_TYPE(Integer.MAX_VALUE, "UNSUPPORTED_TYPE"),
    UNKNOWN(Integer.MAX_VALUE, "UNKNOWN");

    private final int priority;
    private final String name;

    PowerSupplyStatus(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
