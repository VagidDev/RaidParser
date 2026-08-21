package com.unifun.raidparser.service;

import com.unifun.raidparser.controllers.dto.ComponentStatusResponse;
import com.unifun.raidparser.controllers.dto.ServerStatusResponse;
import com.unifun.raidparser.controllers.dto.StatusSummaryResponse;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.component.Severity;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.mapper.ApiStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Выборка и агрегация статусов из кэша для API.
 * Фильтры и сводка живут здесь, а не в контроллере, чтобы их можно было
 * покрыть тестами без поднятия веб-слоя.
 */
@Service
@RequiredArgsConstructor
public class StatusQueryService {

    private final RaidParserService raidParserService;
    private final ApiStatusMapper apiStatusMapper;
    private final Clock clock;

    /**
     * @param component  оставить только серверы с этим компонентом, null — все
     * @param severities оставить серверы с такой severity, пусто — все
     * @param serverName фильтр по части имени сервера, null — все
     */
    public List<ServerStatusResponse> query(HealthType component, Set<Severity> severities, String serverName) {
        return raidParserService.getCachedStatus().stream()
                .map(apiStatusMapper::map)
                .filter(server -> matchesName(server, serverName))
                .map(server -> component == null ? server : onlyComponent(server, component))
                .filter(server -> matchesSeverity(server, severities))
                .sorted(bySeverityThenName())
                .toList();
    }

    public Optional<ServerStatusResponse> findByServerName(String serverName) {
        return raidParserService.getCachedStatus().stream()
                .filter(status -> status.serverName().equalsIgnoreCase(serverName))
                .findFirst()
                .map(apiStatusMapper::map);
    }

    public StatusSummaryResponse summary() {
        List<ServerStatus> cached = raidParserService.getCachedStatus();
        List<ServerStatusResponse> servers = cached.stream()
                .map(apiStatusMapper::map)
                .sorted(bySeverityThenName())
                .toList();

        Map<Severity, Integer> bySeverity = emptyCounters();
        Map<HealthType, Map<Severity, Integer>> byComponent = new EnumMap<>(HealthType.class);
        HealthType.reportable().forEach(healthType -> byComponent.put(healthType, emptyCounters()));

        for (ServerStatusResponse server : servers) {
            bySeverity.merge(server.severity(), 1, Integer::sum);
            server.components().forEach((healthType, component) ->
                    byComponent.get(healthType).merge(component.severity(), 1, Integer::sum));
        }

        List<String> attentionRequired = servers.stream()
                .filter(server -> server.severity() == Severity.CRITICAL || server.severity() == Severity.WARNING)
                .map(ServerStatusResponse::server)
                .toList();

        return new StatusSummaryResponse(clock.instant(), servers.size(), bySeverity, byComponent, attentionRequired);
    }

    private Map<Severity, Integer> emptyCounters() {
        Map<Severity, Integer> counters = new LinkedHashMap<>();
        for (Severity severity : Severity.values()) {
            counters.put(severity, 0);
        }
        return counters;
    }

    private ServerStatusResponse onlyComponent(ServerStatusResponse server, HealthType component) {
        ComponentStatusResponse componentStatus = server.components().get(component);
        return new ServerStatusResponse(
                server.server(),
                componentStatus == null ? Severity.NO_DATA : componentStatus.severity(),
                componentStatus == null ? Map.of() : Map.of(component, componentStatus)
        );
    }

    private boolean matchesName(ServerStatusResponse server, String serverName) {
        return serverName == null || serverName.isBlank()
                || server.server().toLowerCase().contains(serverName.toLowerCase());
    }

    private boolean matchesSeverity(ServerStatusResponse server, Set<Severity> severities) {
        return CollectionUtils.isEmpty(severities) || severities.contains(server.severity());
    }

    private Comparator<ServerStatusResponse> bySeverityThenName() {
        return Comparator.comparing(ServerStatusResponse::severity).thenComparing(ServerStatusResponse::server);
    }
}
