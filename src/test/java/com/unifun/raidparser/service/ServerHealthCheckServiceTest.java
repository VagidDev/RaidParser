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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServerHealthCheckServiceTest {
    @Mock
    private ServersToCheckConfigFileDataHandler serversToCheckConfigFileDataHandler;
    @Mock
    private HostOverviewService hostOverviewService;
    @Mock
    private ServersToCheckConfig serversToCheckConfig;
    @Mock
    private HostExecutorService hostExecutorService;
    @Mock
    private ServerTaskMapper serverTaskMapper;
    private RemoteCommandExecutor remoteCommandExecutor;

    private ServerHealthCheckService serverHealthCheckService;

    @BeforeEach
    void setUp() {

        SshUserConfig sshUserConfig = new SshUserConfig();
        sshUserConfig.setLogin("testuser");
        sshUserConfig.setPassword("password");

        remoteCommandExecutor = new RemoteCommandExecutor(sshUserConfig);

        serverHealthCheckService = new ServerHealthCheckService(
                serversToCheckConfigFileDataHandler,
                remoteCommandExecutor,
                hostExecutorService,
                serversToCheckConfig,
                hostOverviewService,
                serverTaskMapper
        );
    }

    @Test
    void checkServers_SuccessfulCheckedServer() {
        when(serversToCheckConfigFileDataHandler.getHostCommands())
                .thenReturn(List.of(new HostCommand("test-server", "echo 'Hello world!'", HealthType.DRIVE_HEALTH)));
        when(hostOverviewService.getPhysicalServerWithCorrectPortByName(anyString()))
                .thenReturn(new HostInformation("test-server", 2223, "127.0.0.1", "HP", "Proxy"));
        when(serversToCheckConfig.getProxyServerIp())
                .thenReturn("127.0.0.1");

        List<ServerData> completedTasks = serverHealthCheckService.checkServers();

        assertFalse(completedTasks.isEmpty());
        System.out.println("Command output: " + completedTasks.get(0).getRawData(HealthType.DRIVE_HEALTH));
        assertTrue(completedTasks.get(0).getRawData(HealthType.DRIVE_HEALTH).contains("Hello world!"));
    }

    @Test
    void checkServers_ShouldCheckDirectServerSuccessfully() {

        HostCommand hostCommand =
                new HostCommand("direct-server", "uptime", HealthType.DRIVE_HEALTH);

        HostInformation hostInformation =
                new HostInformation("direct-server",
                        22,
                        "192.168.1.10",
                        "Dell",
                        "direct");

        when(serversToCheckConfigFileDataHandler.getHostCommands())
                .thenReturn(List.of(hostCommand));

        when(hostOverviewService.getPhysicalServerWithCorrectPortByName(anyString()))
                .thenReturn(hostInformation);

        RemoteCommandExecutor executorMock = mock(RemoteCommandExecutor.class);

        when(executorMock.execute(
                eq("192.168.1.10"),
                eq(22),
                eq("uptime")
        )).thenReturn("Server uptime");

        serverHealthCheckService = new ServerHealthCheckService(
                serversToCheckConfigFileDataHandler,
                executorMock,
                hostExecutorService,
                serversToCheckConfig,
                hostOverviewService,
                serverTaskMapper
        );

        List<ServerData> result = serverHealthCheckService.checkServers();

        assertEquals(1, result.size());
        assertEquals("Server uptime", result.get(0).getRawData(HealthType.DRIVE_HEALTH));

        verify(executorMock).execute("192.168.1.10", 22, "uptime");
    }

    @Test
    void checkServers_ShouldReturnEmptyServerTaskList_WhenHostInformationIsNull() {
        HostCommand hostCommand =
                new HostCommand("unknown-server", "pwd", HealthType.DRIVE_HEALTH);

        when(serversToCheckConfigFileDataHandler.getHostCommands())
                .thenReturn(List.of(hostCommand));

        when(hostOverviewService.getPhysicalServerWithCorrectPortByName(anyString()))
                .thenReturn(null);

        List<ServerData> result = serverHealthCheckService.checkServers();

        assertEquals(0, result.size());
    }

    @Test
    void getHostsToCheck_ShouldReturnCorrectMap() {

        HostCommand hostCommand =
                new HostCommand("server1", "df -h", HealthType.DRIVE_HEALTH);

        HostInformation hostInformation =
                new HostInformation("server1",
                        22,
                        "192.168.0.1",
                        "HP",
                        "proxy");

        when(hostOverviewService.getPhysicalServerWithCorrectPortByName("server1"))
                .thenReturn(hostInformation);

        var result = serverHealthCheckService
                .getHostsToCheck(List.of(hostCommand));

        assertEquals(1, result.size());

        assertEquals(
                hostInformation,
                result.get(hostCommand)
        );
    }
}