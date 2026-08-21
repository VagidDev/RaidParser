package com.unifun.raidparser.controllers;

import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.controllers.dto.ServerStatusResponse;
import com.unifun.raidparser.controllers.dto.StatusListResponse;
import com.unifun.raidparser.controllers.dto.StatusSummaryResponse;
import com.unifun.raidparser.controllers.error.NotFoundException;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.component.Severity;
import com.unifun.raidparser.service.StatusQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.util.List;
import java.util.Set;

/** Чтение статусов из кэша. Ничего не запускает — только отдаёт то, что уже посчитано. */
@RestController
@Profile(Profiles.SERVER)
@RequestMapping(ApiPaths.BASE + "/status")
@RequiredArgsConstructor
public class StatusController {

    private final StatusQueryService statusQueryService;
    private final Clock clock;

    /**
     * @param component фильтр по компоненту: drive_health, psu_health, battery_health
     * @param severity  одна или несколько severity: CRITICAL, WARNING, NO_DATA, OK
     * @param server    часть имени сервера
     */
    @GetMapping
    public StatusListResponse status(@RequestParam(required = false) HealthType component,
                                     @RequestParam(required = false) Set<Severity> severity,
                                     @RequestParam(required = false) String server) {
        List<ServerStatusResponse> servers = statusQueryService.query(component, severity, server);
        return new StatusListResponse(clock.instant(), servers.size(), servers);
    }

    /** Сводка по кэшу: счётчики по severity и список серверов, требующих внимания. */
    @GetMapping("/summary")
    public StatusSummaryResponse summary() {
        return statusQueryService.summary();
    }

    @GetMapping("/{serverName}")
    public ServerStatusResponse serverStatus(@PathVariable String serverName) {
        return statusQueryService.findByServerName(serverName)
                .orElseThrow(() -> new NotFoundException("Server `" + serverName + "` is not present in the status cache"));
    }
}
