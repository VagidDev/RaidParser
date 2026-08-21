package com.unifun.raidparser.controllers.dto;

import java.time.Instant;

/**
 * @param running идёт ли прогон прямо сейчас: пока true, запуск нового вернёт 409
 */
public record AnalysisStateResponse(
        boolean running,
        LastRun lastRun
) {
    public record LastRun(
            String mode,
            Instant startedAt,
            Instant finishedAt,
            long durationMs,
            int servers
    ) {
    }
}
