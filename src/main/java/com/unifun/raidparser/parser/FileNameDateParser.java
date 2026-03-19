package com.unifun.raidparser.parser;

import com.unifun.raidparser.config.SftpConfiguration;
import com.unifun.raidparser.dto.FileRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FileNameDateParser {
    private final SftpConfiguration sftpConfiguration;
    //TODO: write tests
    public String parseDateFromFileName(String fileName) {
        for (FileRule fileRule : sftpConfiguration.getFileRules()) {
            if (fileName.matches(fileRule.getRegex())) {
                return fileRule.getFormat();
            }
        }

        return "";
    }
}
