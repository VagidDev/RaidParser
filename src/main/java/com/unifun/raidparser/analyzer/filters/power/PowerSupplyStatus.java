package com.unifun.raidparser.analyzer.filters.power;

import com.unifun.raidparser.analyzer.filters.Status;

public enum PowerSupplyStatus implements Status {
    FAILED(0, "Failed"),
    NOT_PRESENT(1, "Power Supply Not Present"),
    UNCLAIMED(2, "Unclaimed"),
    UNKNOWN(3, "UNKNOWN"),
    EMPTY(4, "Empty"),
    OK(Integer.MAX_VALUE, "OK");

    private final int priority;
    private final String name;

    PowerSupplyStatus(int priority, String name) {
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
