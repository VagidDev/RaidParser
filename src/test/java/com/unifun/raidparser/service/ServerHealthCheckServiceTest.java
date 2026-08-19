package com.unifun.raidparser.service;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.config.SshUserConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.HostCommand;
import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.handlers.ServersToCheckConfigFileDataHandler;
import com.unifun.raidparser.mapper.ServerTaskMapper;
import com.unifun.raidparser.util.RemoteCommandExecutor;
import jakarta.validation.constraints.Max;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerHealthCheckServiceTest {
    @Mock
    private ServersToCheckConfigFileDataHandler serversToCheckConfigFileDataHandler;
    @Mock
    private HostOverviewService hostOverviewService;
    @Mock
    private HostExecutorService hostExecutorService;
    @Mock
    private ServerTaskMapper serverTaskMapper;

    private ServerHealthCheckService serverHealthCheckService;

    @BeforeEach
    void setUp() {
        serverHealthCheckService = new ServerHealthCheckService(
                serversToCheckConfigFileDataHandler,
                hostExecutorService,
                hostOverviewService,
                serverTaskMapper
        );
    }

    @Test
    void checkServers_SuccessfulCheckedServer() {
        var hostCommands = List.of(new HostCommand("test-server", "echo 'Hello world!'", HealthType.DRIVE_HEALTH));
        var hostInformationList = List.of(
                new HostInformation("test-server", 2223, "127.0.0.1", "HP", "Proxy")
        );

        when(serversToCheckConfigFileDataHandler.getHostCommands())
                .thenReturn(hostCommands);
        when(hostOverviewService.getPhysicalServersWithCorrectPort())
                .thenReturn(hostInformationList);

        serverHealthCheckService.checkServersParallel();

        verify(serverTaskMapper, times(1)).map(hostCommands, hostInformationList);
        verify(hostExecutorService, times(1)).execute(anyList());
    }

    @Test
    void checkServers_ShouldReturnEmptyServerTaskList_WhenHostInformationIsNull() {
        HostCommand hostCommand =
                new HostCommand("unknown-server", "pwd", HealthType.DRIVE_HEALTH);

        when(serversToCheckConfigFileDataHandler.getHostCommands())
                .thenReturn(List.of(hostCommand));

        when(hostOverviewService.getPhysicalServersWithCorrectPort())
                .thenReturn(null);

        List<ServerData> result = serverHealthCheckService.checkServersParallel();

        assertEquals(0, result.size());
    }
}