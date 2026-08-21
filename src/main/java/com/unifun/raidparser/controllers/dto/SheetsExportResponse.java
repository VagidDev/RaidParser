package com.unifun.raidparser.controllers.dto;

import com.unifun.raidparser.core.component.HealthType;

import java.util.Map;

/**
 * @param success true, только если выгрузились все типы компонентов
 */
public record SheetsExportResponse(boolean success, Map<HealthType, Boolean> exported) {
}
