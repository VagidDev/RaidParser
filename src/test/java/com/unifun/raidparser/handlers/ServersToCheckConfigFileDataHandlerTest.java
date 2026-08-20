package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.CacheConfig;
import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.HostCommand;
import com.unifun.raidparser.parser.ServersToCheckConfigFileParser;
import com.unifun.raidparser.service.CommandValidatorService;
import com.unifun.raidparser.util.FileChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
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
                fileChecker,
                Clock.systemUTC(),
                new CacheConfig()
        );
    }

    @Test
    void getHostCommands_shouldLoadAndFilterValidCommands() {
        List<String> fileData = List.of("line1", "line2");

        HostCommand validCommand = new HostCommand("server1", "cmd1", HealthType.DRIVE_HEALTH);
        HostCommand invalidCommand = new HostCommand("server2", "cmd2", HealthType.DRIVE_HEALTH);

        when(parser.parse(any())).thenReturn(List.of(validCommand, invalidCommand));
        when(validator.isValid("cmd1")).thenReturn(true);
        when(validator.isValid("cmd2")).thenReturn(false);

        List<HostCommand> result = handler.getHostCommands();

        assertEquals(1, result.size());
        assertEquals(validCommand, result.get(0));
    }

    @Test
    void getHostCommands_shouldReturnEmpty_whenFileIsEmpty() throws IOException {
        Files.write(configFile, List.of());

        List<HostCommand> result = handler.getHostCommands();

        assertTrue(result == null || result.isEmpty());
    }

    @Test
    void getHostCommands_shouldNotReload_whenCacheExists() {
        HostCommand command = new HostCommand("server1", "cmd1", HealthType.DRIVE_HEALTH);

        when(parser.parse(any())).thenReturn(List.of(command));
        when(validator.isValid("cmd1")).thenReturn(true);

        List<HostCommand> firstCall = handler.getHostCommands();
        List<HostCommand> secondCall = handler.getHostCommands();

        assertEquals(firstCall, secondCall);

        // parser должен вызваться только один раз
        verify(parser, times(1)).parse(any());
    }

    @Test
    void clear_shouldClearData() {
        HostCommand command = new HostCommand("server1", "cmd1", HealthType.DRIVE_HEALTH);

        when(parser.parse(any())).thenReturn(List.of(command));
        when(validator.isValid("cmd1")).thenReturn(true);

        handler.getHostCommands();
        handler.clear();

        List<HostCommand> result = handler.getHostCommands();

        // после очистки кэша — снова загрузка
        verify(parser, times(2)).parse(any());
        assertNotNull(result);
    }

    @Test
    void getHostCommands_shouldReturnNull_whenFileNotExists() {
        when(fileChecker.ensureFileExists(configFile)).thenReturn(false);

        List<HostCommand> result = handler.getHostCommands();

        assertTrue(result.isEmpty());
    }

    @Test
    void getHostCommands_shouldHandleInvalidCommandsOnly() {
        HostCommand invalid1 = new HostCommand("server1", "bad1", HealthType.DRIVE_HEALTH);
        HostCommand invalid2 = new HostCommand("server2", "bad2", HealthType.DRIVE_HEALTH);

        when(parser.parse(any())).thenReturn(List.of(invalid1, invalid2));
        when(validator.isValid(any())).thenReturn(false);

        List<HostCommand> result = handler.getHostCommands();

        assertTrue(result.isEmpty());
    }
}