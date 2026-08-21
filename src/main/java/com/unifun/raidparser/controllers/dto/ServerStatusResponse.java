package com.unifun.raidparser.controllers.dto;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.component.Severity;

import java.util.Map;

/**
 * @param severity худшая severity среди компонентов — по ней удобно сортировать и красить строку
 */
public record ServerStatusResponse(
        String server,
        Severity severity,
        Map<HealthType, ComponentStatusResponse> components
) {
}
