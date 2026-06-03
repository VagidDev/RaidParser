package com.unifun.raidparser.dto;

public record ReportServerData(
        String serverName,
        String healthStatus,
        String errorText
) {
}
