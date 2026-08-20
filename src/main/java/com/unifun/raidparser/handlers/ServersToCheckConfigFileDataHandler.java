package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.dto.HostCommand;
import com.unifun.raidparser.parser.ServersToCheckConfigFileParser;
import com.unifun.raidparser.service.CommandValidatorService;
import com.unifun.raidparser.util.FileChecker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.file.Path;
import java.util.List;

@Service
public class ServersToCheckConfigFileDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServersToCheckConfigFileDataHandler.class);
    private final ServersToCheckConfigFileParser serversToCheckConfigFileParser;
    private final CommandValidatorService commandValidatorService;
    private final ServersToCheckConfig serversToCheckConfig;
    private final FileChecker fileChecker;

    public ServersToCheckConfigFileDataHandler(
            ServersToCheckConfigFileParser serversToCheckConfigFileParser,
            CommandValidatorService commandValidatorService,
            ServersToCheckConfig serversToCheckConfig,
            FileChecker fileChecker) {
        this.serversToCheckConfigFileParser = serversToCheckConfigFileParser;
        this.commandValidatorService = commandValidatorService;
        this.serversToCheckConfig = serversToCheckConfig;
        this.fileChecker = fileChecker;
    }

    private List<HostCommand> hostCommands;

    private boolean loadServerTasks() {
        Path configFile = Path.of(serversToCheckConfig.getServersToCheckConfigFile());
        if (!fileChecker.ensureFileExists(configFile)) {
            LOGGER.warn("Cannot load server tasks due to config file `{}` does not exist! Creating file... Please complete the config `{}`", configFile, configFile);
            return false;
        }

        this.hostCommands = serversToCheckConfigFileParser.parse(configFile).stream()
                .filter(hostCommand -> commandValidatorService.isValid(hostCommand.getCommand()))
                .toList();

        return true;
    }

    public void clearCache() {
        if (hostCommands != null) {
            LOGGER.warn("Cache is cleared!");
            this.hostCommands = null;
        }
    }

    public List<HostCommand> getHostCommands() {
        if (CollectionUtils.isEmpty(hostCommands)) {
            if (loadServerTasks()) {
                LOGGER.info("Successfully load server task from config file");
            } else {
                LOGGER.warn("Cannot load server task from config file!");
            }
        }
        return hostCommands == null ? List.of() : hostCommands;
    }

}
