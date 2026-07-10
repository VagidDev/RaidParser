package com.unifun.raidparser.dto;

public record ReportServerData(
        String serverName,
        String healthStatus,
        String errorText
) {
    public String getPrettyFormat() {
        return "Server Name: " + serverName
                + " -> " + healthStatus + " -> " + errorText;
    }
}
