package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.CacheConfig;
import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.dto.HostCommand;
import com.unifun.raidparser.parser.ServersToCheckConfigFileParser;
import com.unifun.raidparser.service.CommandValidatorService;
import com.unifun.raidparser.util.CacheExpiry;
import com.unifun.raidparser.util.FileChecker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

@Service
public class ServersToCheckConfigFileDataHandler implements ManagedCache {
    private static final Logger LOGGER = LogManager.getLogger(ServersToCheckConfigFileDataHandler.class);
    private static final String CACHE_NAME = "commands";

    private final ServersToCheckConfigFileParser serversToCheckConfigFileParser;
    private final CommandValidatorService commandValidatorService;
    private final ServersToCheckConfig serversToCheckConfig;
    private final FileChecker fileChecker;
    private final CacheExpiry expiry;

    private List<HostCommand> hostCommands = List.of();

    public ServersToCheckConfigFileDataHandler(
            ServersToCheckConfigFileParser serversToCheckConfigFileParser,
            CommandValidatorService commandValidatorService,
            ServersToCheckConfig serversToCheckConfig,
            FileChecker fileChecker,
            Clock clock,
            CacheConfig cacheConfig) {
        this.serversToCheckConfigFileParser = serversToCheckConfigFileParser;
        this.commandValidatorService = commandValidatorService;
        this.serversToCheckConfig = serversToCheckConfig;
        this.fileChecker = fileChecker;
        this.expiry = new CacheExpiry(clock, cacheConfig.getServersToCheckTtlSeconds());
    }

    public List<HostCommand> getHostCommands() {
        if (CollectionUtils.isEmpty(hostCommands)) {
            loadServerTasks();
            return hostCommands;
        }

        if (expiry.isExpired()) {
            LOGGER.info("Commands were loaded more than {} seconds ago, re-reading the config file", expiry.ttlSeconds());
            loadServerTasks();
        }

        return hostCommands;
    }

    @Override
    public String cacheName() {
        return CACHE_NAME;
    }

    @Override
    public void clear() {
        hostCommands = List.of();
        expiry.invalidate();
        LOGGER.info("Commands cache is cleared");
    }

    @Override
    public CacheState state() {
        return new CacheState(
                CACHE_NAME,
                !hostCommands.isEmpty(),
                hostCommands.size(),
                expiry.age(),
                expiry.ttlSeconds(),
                expiry.isExpired()
        );
    }

    private void loadServerTasks() {
        Path configFile = Path.of(serversToCheckConfig.getServersToCheckConfigFile());
        if (!fileChecker.ensureFileExists(configFile)) {
            LOGGER.warn("Cannot load server tasks due to config file `{}` does not exist! Creating file... Please complete the config `{}`", configFile, configFile);
            return;
        }

        hostCommands = serversToCheckConfigFileParser.parse(configFile).stream()
                .filter(hostCommand -> commandValidatorService.isValid(hostCommand.getCommand()))
                .toList();
        expiry.markLoaded();

        if (hostCommands.isEmpty()) {
            LOGGER.warn("No allowed commands loaded from config file `{}`", configFile);
        } else {
            LOGGER.info("Successfully loaded {} command(s) from config file", hostCommands.size());
        }
    }
}
