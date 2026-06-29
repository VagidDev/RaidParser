package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("sftp.remote.file-rule")
@Getter @Setter
public class RemoteFileRuleConfig {
    private String directory;
    private String mask;
    private String dateFormat;

    public String getRegex() {
        return "^" + mask
                .replace(".", "\\.")
                .replace("{date}", "(.*)") + "$";
    }
}
