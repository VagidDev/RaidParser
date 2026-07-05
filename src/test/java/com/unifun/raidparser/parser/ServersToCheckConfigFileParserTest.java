package com.unifun.raidparser.parser;

import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.HostCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ServersToCheckConfigFileParserTest {

    private final ServersToCheckConfigFileParser parser = new ServersToCheckConfigFileParser();

    @TempDir
    Path tempDir;

    private Path writeYaml(String content) throws IOException {
        Path file = tempDir.resolve("config.yaml");
        Files.writeString(file, content);
        return file;
    }

    @Test
    void parsesValidConfigWithAllCommandTypes() throws IOException {
        String yaml = """
                commands:
                  - host: "test_host_01"
                    command: "cat /proc/mdstat"
                    type: "drive_health"
                  - host: "test_host_02"
                    command: "sudo dmidecode -t 39"
                    type: "psu_health"
                  - host: "test_host_01"
                    command: "sudo ssacli ctrl all show detail"
                    type: "battery_health"
                """;
        Path configFile = writeYaml(yaml);

        List<HostCommand> result = parser.parse(configFile);

        assertThat(result).hasSize(3);
        assertThat(result.get(0))
                .isEqualTo(new HostCommand("test_host_01", "cat /proc/mdstat", HealthType.DRIVE_HEALTH));
        assertThat(result.get(1).getType()).isEqualTo(HealthType.PSU_HEALTH);
        assertThat(result.get(2).getType()).isEqualTo(HealthType.BATTERY_HEALTH);
    }

    @Test
    void parsesConfigWithMultipleHostsForSameCommandType() throws IOException {
        String yaml = """
                commands:
                  - host: "test_host_01"
                    command: "cat /proc/mdstat"
                    type: "drive_health"
                  - host: "test_host_02"
                    command: "cat /proc/mdstat"
                    type: "drive_health"
                """;
        Path configFile = writeYaml(yaml);

        List<HostCommand> result = parser.parse(configFile);

        assertThat(result).hasSize(2)
                .extracting(HostCommand::getHost)
                .containsExactly("test_host_01", "test_host_02");
    }

    @Test
    void returnsEmptyListWhenCommandsListIsEmpty() throws IOException {
        Path configFile = writeYaml("commands: []\n");

        List<HostCommand> result = parser.parse(configFile);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsNullWhenCommandsKeyIsMissing_documentsCurrentBehavior() throws IOException {
        Path configFile = writeYaml("someOtherKey: value\n");

        List<HostCommand> result = parser.parse(configFile);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyListWhenFileIsCompletelyEmpty() throws IOException {
        Path configFile = writeYaml("");

        List<HostCommand> result = parser.parse(configFile);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyListWhenFileHasInvalidYamlSyntax() throws IOException {
        Path configFile = writeYaml("""
                commands:
                  - host: "test_host_01"
                  command: "broken indentation"
                    type: "drive_health"
                """);

        List<HostCommand> result = parser.parse(configFile);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyListWhenTypeValueIsUnrecognized() throws IOException {
        Path configFile = writeYaml("""
                commands:
                  - host: "test_host_01"
                    command: "cat /proc/mdstat"
                    type: "unknown_health"
                """);

        List<HostCommand> result = parser.parse(configFile);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsEmptyListWhenFileDoesNotExist() {
        Path configFile = tempDir.resolve("does_not_exist.yaml");

        List<HostCommand> result = parser.parse(configFile);

        assertThat(result).isEmpty();
    }

    @Test
    void parsesCommandWithMissingHostFieldAsNull() throws IOException {
        Path configFile = writeYaml("""
                commands:
                  - command: "cat /proc/mdstat"
                    type: "drive_health"
                """);

        List<HostCommand> result = parser.parse(configFile);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHost()).isNull();
        assertThat(result.get(0).getCommand()).isEqualTo("cat /proc/mdstat");
        assertThat(result.get(0).getType()).isEqualTo(HealthType.DRIVE_HEALTH);
    }
}