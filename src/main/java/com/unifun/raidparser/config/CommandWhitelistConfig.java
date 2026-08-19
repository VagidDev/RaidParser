package com.unifun.raidparser.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.regex.Pattern;

@Configuration
@ConfigurationProperties("util.commands")
@Getter @Setter
public class CommandWhitelistConfig {
    private List<String> whitelist;

    @Bean
    public List<Pattern> commandPatterns() {
        return whitelist.stream().map(Pattern::compile).toList();
    }
}
