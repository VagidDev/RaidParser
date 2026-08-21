package com.unifun.raidparser.core.filters;

import com.unifun.raidparser.core.component.Severity;

public interface Status {
    /** Меньшее значение — более серьёзная проблема. */
    int getPriority();

    String getName();

    /** Обобщённая классификация для API и отчётов. */
    Severity getSeverity();

    String toString();
}
