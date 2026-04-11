package com.unifun.raidparser.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileChecker {
    private static final Logger LOGGER = LogManager.getLogger(FileChecker.class);
    public boolean ensureFileExists(Path file) {
        if (Files.exists(file)) {
            LOGGER.info("File exists!");
            return true;
        }
        LOGGER.warn("File does not exists! Creating file {}", file);
        try {
            Files.createDirectories(file.getParent());
            Files.createFile(file);
            LOGGER.info("File {} is created!", file);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while creating file. Error -> {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean ensureDirectoryExists(Path directory) {
        if (Files.exists(directory)) {
            LOGGER.info("Directory {} exists", directory);
            return true;
        }

        try {
            LOGGER.warn("Directory {} does not exists! Creating directory...", directory);
            Files.createDirectories(directory);
            return true;
        } catch (IOException e) {
            LOGGER.error("Cannot create directory {} due to {}", directory, e.getLocalizedMessage(), e);
            return false;
        }
    }
}
