package com.unifun.raidparser.parser;

import com.unifun.raidparser.dto.ServerTask;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServersToCheckConfigFileParserTest {

    private final ServersToCheckConfigFileParser parser = new ServersToCheckConfigFileParser();

    @Test
    void parse_shouldParseAllValidLines() {
        List<String> input = List.of(
                "server1 -> echo 1",
                "server2 -> echo 2"
        );

        List<ServerTask> result = parser.parse(input);

        assertEquals(2, result.size());
        assertEquals(new ServerTask("server1", "echo 1"), result.get(0));
        assertEquals(new ServerTask("server2", "echo 2"), result.get(1));
    }

    @Test
    void parse_shouldSkipInvalidLines() {
        List<String> input = List.of(
                "server1 -> echo 1",
                "invalid line without delimiter",
                "server2 -> echo 2"
        );

        List<ServerTask> result = parser.parse(input);

        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(task -> task.getServerName().contains("invalid")));
    }

    @Test
    void parse_shouldReturnEmptyList_whenAllLinesInvalid() {
        List<String> input = List.of(
                "invalid1",
                "invalid2",
                "another bad line"
        );

        List<ServerTask> result = parser.parse(input);

        assertTrue(result.isEmpty());
    }

    @Test
    void parse_shouldTrimValues() {
        List<String> input = List.of(
                "   server1    ->    echo test   "
        );

        List<ServerTask> result = parser.parse(input);

        assertEquals(1, result.size());
        assertEquals("server1", result.get(0).getServerName());
        assertEquals("echo test", result.get(0).getCommandToExecute());
    }

    @Test
    void parse_shouldSkipLinesWithEmptyParts() {
        List<String> input = List.of(
                " -> echo test",
                "server1 -> ",
                "   ->   "
        );

        List<ServerTask> result = parser.parse(input);
        //TODO: fix this
        assertTrue(result.isEmpty());
    }

    @Test
    void parse_shouldHandleMixedValidAndInvalidLines() {
        List<String> input = List.of(
                "server1 -> echo 1",
                "bad line",
                "server2 -> echo 2",
                " -> broken",
                "server3 -> echo 3"
        );

        List<ServerTask> result = parser.parse(input);
        //TODO: fix this
        assertEquals(3, result.size());
    }

    @Test
    void parse_shouldReturnEmptyList_whenInputIsEmpty() {
        List<ServerTask> result = parser.parse(List.of());

        assertTrue(result.isEmpty());
    }
}