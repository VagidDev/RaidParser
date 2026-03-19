package com.unifun.raidparser.config;

import com.unifun.raidparser.dto.FileRule;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("sftp.remote")
@Getter @Setter
public class SftpConfiguration {
    private List<FileRule> fileRules;
}
