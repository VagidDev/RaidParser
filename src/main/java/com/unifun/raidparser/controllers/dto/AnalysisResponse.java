package com.unifun.raidparser.controllers.dto;

import java.time.Instant;
import java.util.List;

public record AnalysisResponse(
        String mode,
        Instant startedAt,
        Instant finishedAt,
        long durationMs,
        int servers,
        List<ServerStatusResponse> statuses
) {
}
