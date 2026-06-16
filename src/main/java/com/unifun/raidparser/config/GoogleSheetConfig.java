package com.unifun.raidparser.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

@Configuration
@RequiredArgsConstructor
public class GoogleSheetConfig {
    private static final Logger LOGGER = LogManager.getLogger(GoogleSheetConfig.class);
    private static final String APPLICATION_NAME = "Google Sheets Server Status Exporter";
    private final GoogleSheetAuthorizationConfig authorizationConfig;

    @Bean
    public Sheets sheetsService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(httpTransport, GsonFactory.getDefaultInstance(), getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(NetHttpTransport httpTransport) throws IOException {
        InputStream in = Files.newInputStream(
                Path.of(authorizationConfig.getUserCredentialsJson()), StandardOpenOption.READ);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(GsonFactory.getDefaultInstance(), new InputStreamReader(in));

        List<String> scopes = List.of("https://www.googleapis.com/auth/spreadsheets");

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, GsonFactory.getDefaultInstance(), clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(new File(authorizationConfig.getSavingTokensDir())))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private boolean ensureActualityOfToken(String tokenDir) {
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
            LocalDateTime creationDateTime = LocalDateTime.ofInstant(attributes.creationTime().toInstant(), ZoneId.systemDefault());
            LocalDateTime today = LocalDateTime.now();

            if (authorizationConfig.getTokenLifetimeInDays() > ChronoUnit.DAYS.between(creationDateTime, today)) {
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
