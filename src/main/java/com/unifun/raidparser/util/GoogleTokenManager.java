package com.unifun.raidparser.util;

import com.google.api.client.auth.oauth2.Credential;
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

    public void validateCredential(Credential credential) throws IOException {
        if (credential.getRefreshToken() == null) {
            throw new IOException(
                    "Google OAuth refresh token is missing. Reauthorization is required."
            );
        }

        Long expiresInSeconds = credential.getExpiresInSeconds();

        boolean tokenMissing = credential.getAccessToken() == null;
        boolean tokenExpiresSoon =
                expiresInSeconds != null && expiresInSeconds <= 120;

        if (!tokenMissing && !tokenExpiresSoon) {
            LOGGER.info(
                    "Google access token is valid for approximately {} seconds",
                    expiresInSeconds
            );
            return;
        }

        LOGGER.info("Google access token is missing or expires soon. Refreshing it.");

        boolean refreshed = credential.refreshToken();

        if (!refreshed) {
            throw new IOException(
                    "Google access token could not be refreshed. Reauthorization is required."
            );
        }

        LOGGER.info("Google access token was successfully refreshed.");
    }
}
