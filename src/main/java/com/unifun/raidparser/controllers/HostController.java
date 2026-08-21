package com.unifun.raidparser.controllers;

import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.controllers.dto.HostResponse;
import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.service.HostOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Список серверов из HostOverview: то, по чему потом ходит проверка. */
@RestController
@Profile(Profiles.SERVER)
@RequestMapping(ApiPaths.BASE + "/hosts")
@RequiredArgsConstructor
public class HostController {

    private final HostOverviewService hostOverviewService;

    /**
     * @param onlyPhysical только физические серверы с корректным портом — те, которые проверяются по SSH
     */
    @GetMapping
    public List<HostResponse> hosts(@RequestParam(defaultValue = "true") boolean onlyPhysical) {
        List<HostInformation> hosts = onlyPhysical
                ? hostOverviewService.getPhysicalServersWithCorrectPort()
                : hostOverviewService.getServers();
        return hosts.stream().map(this::toResponse).toList();
    }

    /** Перечитать список из HostOverview, минуя кэш. */
    @PostMapping("/refresh")
    public List<HostResponse> refresh() {
        return hostOverviewService.getActualServers().stream().map(this::toResponse).toList();
    }

    private HostResponse toResponse(HostInformation host) {
        return new HostResponse(host.getName(), host.getIp(), host.getPort(), host.getServerType(), host.getConnectionType());
    }
}
