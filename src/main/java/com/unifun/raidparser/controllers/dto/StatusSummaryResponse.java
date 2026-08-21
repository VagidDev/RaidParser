package com.unifun.raidparser.controllers.dto;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.component.Severity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Сводка по кэшу — то, из чего собирается дашборд.
 *
 * @param bySeverity          сколько серверов в каждой severity по худшему компоненту
 * @param byComponent         разбивка по каждому типу компонента
 * @param attentionRequired   серверы с CRITICAL или WARNING, худшие первыми
 */
public record StatusSummaryResponse(
        Instant generatedAt,
        int servers,
        Map<Severity, Integer> bySeverity,
        Map<HealthType, Map<Severity, Integer>> byComponent,
        List<String> attentionRequired
) {
}
