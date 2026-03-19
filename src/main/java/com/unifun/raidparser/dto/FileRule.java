package com.unifun.raidparser.dto;

import lombok.Data;

@Data
public class FileRule {
    private String mask;
    private String format;

    public String getRegex() {
        return "^" + mask
                .replace(".", "\\.")
                .replace("{date}", "(.*)") + "$";
    }
}
