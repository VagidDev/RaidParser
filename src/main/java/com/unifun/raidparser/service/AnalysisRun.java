package com.unifun.raidparser.service;

import com.unifun.raidparser.dto.ServerStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Результат одного прогона анализа.
 *
 * @param mode       что запускали: report, hosts или full
 * @param statuses   статусы, полученные этим прогоном
 */
public record AnalysisRun(
        String mode,
        Instant startedAt,
        Instant finishedAt,
        List<ServerStatus> statuses
) {
    public long durationMs() {
        return Duration.between(startedAt, finishedAt).toMillis();
    }

    public int servers() {
        return statuses.size();
    }
}
