package com.unifun.raidparser.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.unifun.raidparser.util.GoogleTokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
@RequiredArgsConstructor
public class GoogleSheetConfig {
    private static final String APPLICATION_NAME = "Google Sheets Server Status Exporter";
    private final GoogleSheetAuthorizationConfig authorizationConfig;
    private final GoogleTokenManager googleTokenManager;

    /**
     * Клиент создаётся лениво: авторизация в Google требует настроенных креденшелов,
     * и её отсутствие не должно мешать парсингу отчётов и проверке хостов.
     * Забирать бин следует через ObjectProvider, иначе синглтон-потребитель
     * снова потянет создание при старте контекста.
     */
    @Bean
    @Lazy
    public Sheets sheetsService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Sheets.Builder(
                httpTransport,
                GsonFactory.getDefaultInstance(),
                googleTokenManager.getCredentials(
                        httpTransport,
                        authorizationConfig.getUserCredentialsJson(),
                        authorizationConfig.getSavingTokensDir()
                )
        )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
}
