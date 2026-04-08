package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.ServersToCheckConfig;
import com.unifun.raidparser.dto.ServerTask;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServersToCheckConfigFileDataHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServersToCheckConfigFileDataHandler.class);
    private final ServersToCheckConfig serversToCheckConfig;

    private List<ServerTask> serverTaskList;

    private boolean loadServerTasks() {
        Path configFile = Path.of(serversToCheckConfig.getServersToCheckConfigFile());

        return true;
    }

    private boolean ensureConfigExists(Path configFile) {
        if (Files.exists(configFile)) {
            return true;
        }

        if(!Files.exists(configFile.getParent())) {
            try {
                Files.createDirectories(configFile.getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Files.createFile(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //need to remove
        return false;
    }
}
