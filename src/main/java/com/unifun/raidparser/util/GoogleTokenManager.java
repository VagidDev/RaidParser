package com.unifun.raidparser.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

@Service
public class GoogleTokenManager {
    private static final Logger LOGGER = LogManager.getLogger(GoogleTokenManager.class);

    @Deprecated
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

    public Credential getCredentials(NetHttpTransport httpTransport, String userCredentialsJson, String savingTokenDir) {
        try {
            return initializeCredentials(httpTransport, userCredentialsJson, savingTokenDir);
        } catch (IOException e) {
            LOGGER.warn("Receiver an exception -> {}. Remove stored credentials in directory {}", e.getLocalizedMessage(), savingTokenDir);
            removeTokens(savingTokenDir);
            try {
                LOGGER.warn("Trying to reinitialize credentials");
                return initializeCredentials(httpTransport, userCredentialsJson, savingTokenDir);
            } catch (IOException exception) {
                LOGGER.error("Fatal error while trying to initialize credentials. Error -> {}", exception.getLocalizedMessage(), e);
                throw new RuntimeException("Cannot initialize credentials for google sheets export", exception);
            }
        }
    }

    private Credential initializeCredentials(NetHttpTransport httpTransport, String userCredentialsJson, String savingTokenDir) throws IOException {
        GoogleClientSecrets clientSecrets;

        try (InputStream in = Files.newInputStream(
                Path.of(userCredentialsJson),
                StandardOpenOption.READ)) {

            clientSecrets = GoogleClientSecrets.load(
                    GsonFactory.getDefaultInstance(),
                    new InputStreamReader(in)
            );
        }

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        httpTransport,
                        GsonFactory.getDefaultInstance(),
                        clientSecrets,
                        List.of("https://www.googleapis.com/auth/spreadsheets")
                )
                        .setDataStoreFactory(
                                new FileDataStoreFactory(
                                        new File(savingTokenDir)
                                )
                        )
                        .setAccessType("offline")
                        .build();

        LocalServerReceiver receiver =
                new LocalServerReceiver.Builder()
                        .setPort(8888)
                        .build();

        return new AuthorizationCodeInstalledApp(flow, receiver)
                        .authorize("user");
    }

    private void removeTokens(String savingTokenDir) {
        Path tokenDirPath = Path.of(savingTokenDir);

        try (Stream<Path> storedTokens = Files.walk(tokenDirPath)) {
            Path token = storedTokens
                    .filter(path -> path.compareTo(tokenDirPath) != 0)
                    .findFirst()
                    .orElse(null);

            if (token == null) {
                LOGGER.info("Token in directory `{}` does not exists, can create new token", tokenDirPath);
                return;
            }

            boolean isDeleted = Files.deleteIfExists(token);
            if (isDeleted) {
                LOGGER.info("Token was deleted: {} ", token.getFileName());
            } else {
                LOGGER.error("Cannot delete Token: {}", token.getFileName());
            }
        } catch (IOException e) {
            LOGGER.error("Input/Output exception while deleting old credential! Path to token -> {}, StackTrace -> {}", tokenDirPath, e.getMessage(), e);
        }
    }

}
