package com.unifun.raidparser.controllers.dto;

import java.time.Instant;
import java.util.List;

public record StatusListResponse(
        Instant generatedAt,
        int count,
        List<ServerStatusResponse> servers
) {
}
