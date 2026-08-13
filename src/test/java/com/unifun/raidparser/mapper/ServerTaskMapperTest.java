package com.unifun.raidparser.mapper;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.HostCommand;
import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.dto.ServerTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServerTaskMapperTest {
    @Mock
    private ServersToCheckConfig serversToCheckConfig;

    @InjectMocks
    private ServerTaskMapper serverTaskMapper;

    private static final String PROXY_IP = "192.168.1.100";

    private List<HostCommand> setCommands() {
        return List.of(
                new HostCommand("test", "cat /proc/mdstat", HealthType.DRIVE_HEALTH),
                new HostCommand("test", "dmidecode -t psu", HealthType.PSU_HEALTH),
                new HostCommand("test", "ssacli ctl all show detail", HealthType.BATTERY_HEALTH)
        );
    }

    private List<HostInformation> setHostInformaionList() {
        return List.of(
                new HostInformation(
                        "test",
                        2568,
                        "10.0.0.2",
                        "VM ware",
                        "proxy"
                ),
                new HostInformation(
                        "test2",
                        2211,
                        "10.0.0.3",
                        "VM ware",
                        "proxy"
                )
        );
    }

    @Test
    void map_successfulCountOfServer() {
        List<ServerTask> tasks = serverTaskMapper.map(setCommands(), setHostInformaionList());
        assertEquals(1, tasks.size());
    }

    @Test
    void map_successfulCountOfServerCommandsForExecution() {
        List<ServerTask> tasks = serverTaskMapper.map(setCommands(), setHostInformaionList());
        assertEquals(3, tasks.get(0).healthCommand().size());
    }

    @Test
    void map_shouldBeProxyIp() {
        when(serversToCheckConfig.getProxyServerIp()).thenReturn(PROXY_IP);
        List<ServerTask> tasks = serverTaskMapper.map(setCommands(), setHostInformaionList());
        assertEquals(serversToCheckConfig.getProxyServerIp(), tasks.get(0).ip());
    }

    @Test
    void map_shouldBeServerIp() {
        List<HostInformation> hostInformationList = List.of(
                new HostInformation(
                        "test",
                        22,
                        "10.0.0.2",
                        "VM ware",
                        "local"
                )
        );

        List<ServerTask> tasks = serverTaskMapper.map(setCommands(), hostInformationList);
        assertEquals("10.0.0.2", tasks.get(0).ip());
    }

    @Test
    void map_ShouldReturnEmptyList_WhenInputListsAreNullOrEmpty() {
        // Проверка на null списки
        List<ServerTask> resultNull = serverTaskMapper.map(null, null);
        assertTrue(resultNull.isEmpty());

        // Проверка на пустые списки
        List<ServerTask> resultEmpty = serverTaskMapper.map(Collections.emptyList(), Collections.emptyList());
        assertTrue(resultEmpty.isEmpty());

        // Проверка, если один из списков пустой
        List<HostCommand> commands = List.of(new HostCommand("host1", "ls", HealthType.BATTERY_HEALTH));
        List<ServerTask> resultOneEmpty = serverTaskMapper.map(commands, Collections.emptyList());
        assertTrue(resultOneEmpty.isEmpty());
    }

    @Test
    void map_ShouldMapDirectConnection_WhenConnectionTypeIsNotProxy() {
        // Given
        HostCommand command = new HostCommand("server-1", "df -h", HealthType.DRIVE_HEALTH);
        HostInformation info = new HostInformation("server-1", 8080, "10.0.0.5", "LINUX", "direct");

        // proxyIp настраиваем, так как buildServerTask инициализирует им переменную по умолчанию
        when(serversToCheckConfig.getProxyServerIp()).thenReturn(PROXY_IP);

        // When
        List<ServerTask> result = serverTaskMapper.map(List.of(command), List.of(info));

        // Then
        assertEquals(1, result.size());
        ServerTask task = result.get(0);
        assertEquals("server-1", task.hostname());
        assertEquals("10.0.0.5", task.ip()); // Для direct соединения берется IP хоста
        assertEquals(22, task.port());       // Для direct порт жестко зашит как 22
        assertEquals(1, task.healthCommand().size());
        assertEquals("df -h", task.healthCommand().get(HealthType.DRIVE_HEALTH));
    }

    @Test
    void map_ShouldMapProxyConnection_WhenConnectionTypeIsProxy() {
        // Given
        HostCommand command = new HostCommand("server-2", "top -b -n 1", HealthType.BATTERY_HEALTH);
        HostInformation info = new HostInformation("server-2", 2222, "10.0.0.6", "LINUX", "proxy");

        when(serversToCheckConfig.getProxyServerIp()).thenReturn(PROXY_IP);

        // When
        List<ServerTask> result = serverTaskMapper.map(List.of(command), List.of(info));

        // Then
        assertEquals(1, result.size());
        ServerTask task = result.get(0);
        assertEquals("server-2", task.hostname());
        assertEquals(PROXY_IP, task.ip()); // Для proxy соединения берется прокси IP из конфига
        assertEquals(2222, task.port());   // И порт из HostInformation
        assertEquals("top -b -n 1", task.healthCommand().get(HealthType.BATTERY_HEALTH));
    }

    @Test
    void map_ShouldGroupCommands_WhenMultipleCommandsForSameHost() {
        // Given
        HostCommand command1 = new HostCommand("server-1", "df -h", HealthType.DRIVE_HEALTH);
        HostCommand command2 = new HostCommand("server-1", "top", HealthType.BATTERY_HEALTH);

        // Повторяющаяся команда с тем же типом (не должна перезаписать первую из-за putIfAbsent)
        HostCommand command3 = new HostCommand("server-1", "another-df", HealthType.DRIVE_HEALTH);

        HostInformation info = new HostInformation("server-1", 22, "10.0.0.5", "LINUX", "direct");

        when(serversToCheckConfig.getProxyServerIp()).thenReturn(PROXY_IP);

        // When
        List<ServerTask> result = serverTaskMapper.map(List.of(command1, command2, command3), List.of(info));

        // Then
        assertEquals(1, result.size());
        ServerTask task = result.get(0);
        assertEquals("server-1", task.hostname());
        assertEquals(2, task.healthCommand().size()); // Должно быть только 2 уникальных типа
        assertEquals("df -h", task.healthCommand().get(HealthType.DRIVE_HEALTH)); // Первая команда сохранилась
        assertEquals("top", task.healthCommand().get(HealthType.BATTERY_HEALTH));
    }

    @Test
    void map_ShouldIgnoreCase_WhenMatchingHostnames() {
        // Given
        // В команде имя "SERVER-UPPER", а в информации "server-upper"
        HostCommand command = new HostCommand("SERVER-UPPER", "uptime", HealthType.BATTERY_HEALTH);
        HostInformation info = new HostInformation("server-upper", 22, "10.0.0.7", "LINUX", "direct");

        when(serversToCheckConfig.getProxyServerIp()).thenReturn(PROXY_IP);

        // When
        List<ServerTask> result = serverTaskMapper.map(List.of(command), List.of(info));

        // Then
        assertEquals(1, result.size());
        // Так как ключ берется из hostCommand.getHost(), имя будет в верхнем регистре
        assertEquals("SERVER-UPPER", result.get(0).hostname());
    }

    @Test
    void map_ShouldIgnoreCommand_WhenNoMatchingHostInformation() {
        // Given
        HostCommand command = new HostCommand("unknown-server", "uptime", HealthType.BATTERY_HEALTH);
        HostInformation info = new HostInformation("server-1", 22, "10.0.0.5", "LINUX", "direct");

        // When
        List<ServerTask> result = serverTaskMapper.map(List.of(command), List.of(info));

        // Then
        assertTrue(result.isEmpty()); // Соответствий нет, мапа должна остаться пустой
    }

}