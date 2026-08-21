package com.unifun.raidparser.controllers.dto;

public record HostResponse(
        String name,
        String ip,
        int port,
        String serverType,
        String connectionType
) {
}
