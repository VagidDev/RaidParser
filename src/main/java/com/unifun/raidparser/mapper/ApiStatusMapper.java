package com.unifun.raidparser.mapper;

import com.unifun.raidparser.controllers.dto.ComponentStatusResponse;
import com.unifun.raidparser.controllers.dto.ServerStatusResponse;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.component.Severity;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

/** Преобразование внутренних статусов в ответы API. */
@Service
public class ApiStatusMapper {

    public ServerStatusResponse map(ServerStatus serverStatus) {
        Map<HealthType, ComponentStatusResponse> components = new EnumMap<>(HealthType.class);

        for (HealthType healthType : HealthType.reportable()) {
            AnalyzeResponse<? extends Status> response = serverStatus.healthStatusMap().get(healthType);
            components.put(healthType, mapComponent(response));
        }

        return new ServerStatusResponse(serverStatus.serverName(), worstSeverity(components), components);
    }

    /** Отсутствие данных по компоненту — тоже ответ, поэтому попадает в вывод как NO_DATA. */
    private ComponentStatusResponse mapComponent(AnalyzeResponse<? extends Status> response) {
        if (response == null || response.getStatus() == null) {
            return new ComponentStatusResponse("NO DATA", Severity.NO_DATA, Integer.MAX_VALUE, "");
        }

        Status status = response.getStatus();
        String details = response.getErrorText() == null ? "" : response.getErrorText();
        return new ComponentStatusResponse(status.getName(), status.getSeverity(), status.getPriority(), details);
    }

    private Severity worstSeverity(Map<HealthType, ComponentStatusResponse> components) {
        return components.values().stream()
                .map(ComponentStatusResponse::severity)
                .min(Comparator.naturalOrder())
                .orElse(Severity.NO_DATA);
    }
}
