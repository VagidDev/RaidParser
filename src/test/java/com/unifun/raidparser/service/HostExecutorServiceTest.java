package com.unifun.raidparser.service;

import com.unifun.raidparser.config.HostExecutorConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerTask;
import com.unifun.raidparser.util.RemoteCommandExecutor;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HostExecutorServiceTest {

    @Mock
    private RemoteCommandExecutor remoteCommandExecutor;

    @Mock
    private HostExecutorConfig hostExecutorConfig;

    @InjectMocks
    private HostExecutorService hostExecutorService;

    @BeforeEach
    public void initialize() {
        when(hostExecutorConfig.getThreads()).thenReturn(8);
        hostExecutorService.initialize();
    }

    @AfterEach
    public void destroy() {
        hostExecutorService.destroy();
    }

    @Test
    void execute_ShouldReturnServerData_WhenTasksAreExecutedSuccessfully() {
        // Given
        ServerTask task1 = new ServerTask("server-1", "10.0.0.1", 22, Map.of(HealthType.BATTERY_HEALTH, "top"));
        ServerTask task2 = new ServerTask("server-2", "10.0.0.2", 2222, Map.of(HealthType.DRIVE_HEALTH, "df -h"));

        // Настраиваем мок на успешные ответы
        when(remoteCommandExecutor.execute("10.0.0.1", 22, "top")).thenReturn("cpu load 10%");
        when(remoteCommandExecutor.execute("10.0.0.2", 2222, "df -h")).thenReturn("disk free 50G");

        // When
        List<ServerData> result = hostExecutorService.execute(List.of(task1, task2));

        // Then
        assertEquals(2, result.size());

        // Проверяем первый сервер
        ServerData server1Data = result.stream().filter(s -> s.serverName().equals("server-1")).findFirst().get();
        assertEquals("cpu load 10%", server1Data.rawDataByComponent().get(HealthType.BATTERY_HEALTH));

        // Проверяем второй сервер
        ServerData server2Data = result.stream().filter(s -> s.serverName().equals("server-2")).findFirst().get();
        assertEquals("disk free 50G", server2Data.rawDataByComponent().get(HealthType.DRIVE_HEALTH));

        // Проверяем, что экзекьютор был вызван 2 раза с правильными параметрами
        verify(remoteCommandExecutor, times(1)).execute("10.0.0.1", 22, "top");
        verify(remoteCommandExecutor, times(1)).execute("10.0.0.2", 2222, "df -h");
    }

    @Test
    void execute_ShouldReturnEmptyList_WhenInputIsEmpty() {
        // When
        List<ServerData> result = hostExecutorService.execute(Collections.emptyList());

        // Then
        assertTrue(result.isEmpty());
        verify(remoteCommandExecutor, never()).execute(anyString(), anyInt(), anyString());
    }

    @Test
    void execute_ShouldContinueProcessing_WhenOneTaskThrowsException() {
        // Given
        // Сервер 1 выбросит ошибку при выполнении команды
        ServerTask task1 = new ServerTask("server-error", "10.0.0.99", 22, Map.of(HealthType.BATTERY_HEALTH, "top"));
        // Сервер 2 отработает успешно
        ServerTask task2 = new ServerTask("server-ok", "10.0.0.2", 22, Map.of(HealthType.DRIVE_HEALTH, "df -h"));

        // Мокаем ошибку
        when(remoteCommandExecutor.execute(eq("10.0.0.99"), anyInt(), anyString()))
                .thenThrow(new RuntimeException("SSH Connection Refused"));

        // Мокаем успешное выполнение
        when(remoteCommandExecutor.execute("10.0.0.2", 22, "df -h"))
                .thenReturn("disk ok");

        // When
        List<ServerData> result = hostExecutorService.execute(List.of(task1, task2));

        // Then
        // ВНИМАНИЕ: Если ты не исправил баг с try-catch (перенес внутрь цикла), этот тест упадет!
        // Ожидаем, что успешный сервер вернул результат, несмотря на падение первого
        assertEquals(1, result.size());
        assertEquals("server-ok", result.get(0).serverName());
        assertEquals("disk ok", result.get(0).rawDataByComponent().get(HealthType.DRIVE_HEALTH));
    }
}