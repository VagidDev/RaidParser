package com.unifun.raidparser.parser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FileNameDateParser {
    /**
     * Extracting date format from the name of the file, pattern of the file if set up in configuration
     * @param fileName - name of the file with date format
     * @return - format of the date extracted from file name, or empty
     */
    //TODO: write tests
    // No need to parse date format is you already entered it in config, my bad
//    public String parseDateFromFileName(String fileName) {
//        for (FileRule fileRule : sftpConfiguration.getFileRules()) {
//            if (fileName.matches(fileRule.getRegex())) {
//                return fileRule.getFormat();
//            }
//        }
//
//        return "";
//    }
}
