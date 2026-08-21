package com.unifun.raidparser.controllers.dto;

import com.unifun.raidparser.core.component.HealthType;

import java.util.Map;

public record FileExportResponse(Map<HealthType, String> files) {
}
