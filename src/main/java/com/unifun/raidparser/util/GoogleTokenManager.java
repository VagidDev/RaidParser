package com.unifun.raidparser.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Service
public class GoogleTokenManager {
    private static final Logger LOGGER = LogManager.getLogger(GoogleTokenManager.class);

    public boolean ensureActualityOfToken(String tokenDir, int tokenLifetimeDays) {
        if (!StringUtils.hasText(tokenDir)) {
            LOGGER.warn("Cannot ensure token validity in directory `{}`", tokenDir);
            return false;
        }

        Path tokenDirPath = Path.of(tokenDir);

        try (Stream<Path> storedTokens = Files.walk(tokenDirPath)) {
            Path token = storedTokens
                    .filter(path -> path.compareTo(tokenDirPath) != 0)
                    .findFirst()
                    .orElse(null);

            if (token == null) {
                LOGGER.info("Token in directory `{}` does not exists, can create new token", tokenDirPath);
                return true;
            }

            BasicFileAttributes attributes = Files.readAttributes(token, BasicFileAttributes.class);
            LocalDateTime creationDateTime = LocalDateTime.ofInstant(attributes.lastModifiedTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime today = LocalDateTime.now();

            if (tokenLifetimeDays > ChronoUnit.DAYS.between(creationDateTime, today)) {
                LOGGER.info("Token `{}` is valid. Token creation time -> {}", token, creationDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
                return true;
            }

            boolean isDeleted = Files.deleteIfExists(token);
            if (isDeleted) {
                LOGGER.info("Token was deleted: {} ", token.getFileName());
                return true;
            } else {
                LOGGER.error("Cannot delete Token: {}", token.getFileName());
                return false;
            }
        } catch (IOException e) {
            LOGGER.error("Input/Output exception while deleting old credential! Path to token -> {}, StackTrace -> {}", tokenDirPath, e.getMessage(), e);
            return false;
        }
    }
}
