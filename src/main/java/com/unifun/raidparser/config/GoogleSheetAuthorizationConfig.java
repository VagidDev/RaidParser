package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("sheets.authorization")
@Getter @Setter
public class GoogleSheetAuthorizationConfig {
    private String userCredentialsJson = "./config/credentials.json";
    private String savingTokensDir = "./cache/tokens";
    private int tokenLifetimeInDays = 5;
}
