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
import org.springframework.util.StringUtils;

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
    private static final int RECEIVER_PORT = 8888;

    public Credential getCredentials(NetHttpTransport httpTransport, String userCredentialsJson, String savingTokenDir) {
        Path credentialsFile = resolveCredentialsFile(userCredentialsJson);
        Path tokenDir = resolveTokenDir(savingTokenDir);

        try {
            return initializeCredentials(httpTransport, credentialsFile, tokenDir);
        } catch (IOException e) {
            LOGGER.warn("Received an exception -> {}. Removing stored tokens in directory `{}`", e.getLocalizedMessage(), tokenDir);
            removeTokens(tokenDir);
            try {
                LOGGER.warn("Trying to reinitialize credentials");
                return initializeCredentials(httpTransport, credentialsFile, tokenDir);
            } catch (IOException exception) {
                LOGGER.error("Fatal error while trying to initialize credentials. Error -> {}", exception.getLocalizedMessage(), exception);
                throw new IllegalStateException("Cannot initialize credentials for google sheets export", exception);
            }
        }
    }

    /**
     * Путь к credentials.json проверяем до обращения к Google: пустое значение
     * превращается в Path.of("") — то есть в текущий каталог, и дальше клиент
     * падает с невнятным `Is a directory`.
     */
    private Path resolveCredentialsFile(String userCredentialsJson) {
        if (!StringUtils.hasText(userCredentialsJson)) {
            throw new IllegalStateException(
                    "`sheets.authorization.user-credentials-json` is not configured — cannot authorize in Google Sheets");
        }

        Path credentialsFile = Path.of(userCredentialsJson).toAbsolutePath().normalize();
        if (!Files.isRegularFile(credentialsFile)) {
            throw new IllegalStateException(
                    "Google credentials file `" + credentialsFile + "` does not exist or is not a regular file");
        }
        return credentialsFile;
    }

    /**
     * Каталог токенов тоже обязателен: с пустым значением он указывает на
     * рабочий каталог приложения, а его содержимое {@link #removeTokens} удаляет.
     */
    private Path resolveTokenDir(String savingTokenDir) {
        if (!StringUtils.hasText(savingTokenDir)) {
            throw new IllegalStateException(
                    "`sheets.authorization.saving-tokens-dir` is not configured — refusing to use the working directory for tokens");
        }
        return Path.of(savingTokenDir).toAbsolutePath().normalize();
    }

    private Credential initializeCredentials(NetHttpTransport httpTransport, Path credentialsFile, Path tokenDir) throws IOException {
        GoogleClientSecrets clientSecrets;

        try (InputStream in = Files.newInputStream(credentialsFile, StandardOpenOption.READ)) {
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
                        .setDataStoreFactory(new FileDataStoreFactory(tokenDir.toFile()))
                        .setAccessType("offline")
                        .build();

        LocalServerReceiver receiver =
                new LocalServerReceiver.Builder()
                        .setPort(RECEIVER_PORT)
                        .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Удаляет только обычные файлы непосредственно из каталога токенов.
     * Обход дерева (Files.walk) здесь недопустим: при неверно заданном каталоге
     * под удаление попадал произвольный файл рабочего каталога приложения.
     */
    private void removeTokens(Path tokenDirPath) {
        if (!Files.isDirectory(tokenDirPath)) {
            LOGGER.info("Token directory `{}` does not exist, a new token can be created", tokenDirPath);
            return;
        }

        try (Stream<Path> storedTokens = Files.list(tokenDirPath)) {
            List<Path> tokens = storedTokens.filter(Files::isRegularFile).toList();

            if (tokens.isEmpty()) {
                LOGGER.info("No stored tokens in directory `{}`, a new token can be created", tokenDirPath);
                return;
            }

            for (Path token : tokens) {
                if (Files.deleteIfExists(token)) {
                    LOGGER.info("Token was deleted: {}", token.getFileName());
                } else {
                    LOGGER.error("Cannot delete token: {}", token.getFileName());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Input/Output exception while deleting old credentials! Token directory -> {}, Error -> {}",
                    tokenDirPath, e.getMessage(), e);
        }
    }

}
