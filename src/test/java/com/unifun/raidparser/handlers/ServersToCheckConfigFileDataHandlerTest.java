package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.dto.ServerTask;
import com.unifun.raidparser.parser.ServersToCheckConfigFileParser;
import com.unifun.raidparser.service.CommandValidatorService;
import com.unifun.raidparser.util.FileChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServersToCheckConfigFileDataHandlerTest {

    private ServersToCheckConfigFileParser parser;
    private CommandValidatorService validator;
    private ServersToCheckConfig config;
    private FileChecker fileChecker;

    private ServersToCheckConfigFileDataHandler handler;

    @TempDir
    Path tempDir;

    private Path configFile;

    @BeforeEach
    void setUp() throws IOException {
        parser = mock(ServersToCheckConfigFileParser.class);
        validator = mock(CommandValidatorService.class);
        config = mock(ServersToCheckConfig.class);
        fileChecker = mock(FileChecker.class);

        configFile = tempDir.resolve("config.txt");
        Files.write(configFile, List.of("dummy"));

        when(config.getServersToCheckConfigFile()).thenReturn(configFile.toString());
        when(fileChecker.ensureFileExists(configFile)).thenReturn(true);

        handler = new ServersToCheckConfigFileDataHandler(
                parser,
                validator,
                config,
                fileChecker
        );
    }

    @Test
    void getServerTasks_shouldLoadAndFilterValidCommands() {
        List<String> fileData = List.of("line1", "line2");

        ServerTask validTask = new ServerTask("server1", "cmd1", null);
        ServerTask invalidTask = new ServerTask("server2", "cmd2", null);

        when(parser.parse(any())).thenReturn(List.of(validTask, invalidTask));
        when(validator.isValid("cmd1")).thenReturn(true);
        when(validator.isValid("cmd2")).thenReturn(false);

        List<ServerTask> result = handler.getServerTasks();

        assertEquals(1, result.size());
        assertEquals(validTask, result.get(0));
    }

    @Test
    void getServerTasks_shouldReturnEmpty_whenFileIsEmpty() throws IOException {
        Files.write(configFile, List.of());

        List<ServerTask> result = handler.getServerTasks();

        assertTrue(result == null || result.isEmpty());
    }

    @Test
    void getServerTasks_shouldNotReload_whenCacheExists() {
        ServerTask task = new ServerTask("server1", "cmd1", null);

        when(parser.parse(any())).thenReturn(List.of(task));
        when(validator.isValid("cmd1")).thenReturn(true);

        List<ServerTask> firstCall = handler.getServerTasks();
        List<ServerTask> secondCall = handler.getServerTasks();

        assertEquals(firstCall, secondCall);

        // parser должен вызваться только один раз
        verify(parser, times(1)).parse(any());
    }

    @Test
    void clearCache_shouldClearData() {
        ServerTask task = new ServerTask("server1", "cmd1", null);

        when(parser.parse(any())).thenReturn(List.of(task));
        when(validator.isValid("cmd1")).thenReturn(true);

        handler.getServerTasks();
        handler.clearCache();

        List<ServerTask> result = handler.getServerTasks();

        // после очистки кэша — снова загрузка
        verify(parser, times(2)).parse(any());
        assertNotNull(result);
    }

    @Test
    void getServerTasks_shouldReturnNull_whenFileNotExists() {
        when(fileChecker.ensureFileExists(configFile)).thenReturn(false);

        List<ServerTask> result = handler.getServerTasks();

        assertTrue(result.isEmpty());
    }

    @Test
    void getServerTasks_shouldHandleInvalidCommandsOnly() {
        ServerTask invalid1 = new ServerTask("server1", "bad1", null);
        ServerTask invalid2 = new ServerTask("server2", "bad2", null);

        when(parser.parse(any())).thenReturn(List.of(invalid1, invalid2));
        when(validator.isValid(any())).thenReturn(false);

        List<ServerTask> result = handler.getServerTasks();

        assertTrue(result.isEmpty());
    }
}