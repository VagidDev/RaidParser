package com.unifun.raidparser.dto;


import com.unifun.raidparser.core.component.HealthType;

import java.util.Map;

public record ServerTask(
        String hostname,
        String ip,
        int port,
        Map<HealthType, String> healthCommand
) {
}
