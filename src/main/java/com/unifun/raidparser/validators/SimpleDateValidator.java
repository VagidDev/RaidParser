package com.unifun.raidparser.validators;

import java.util.List;

public class SimpleDateValidator implements SimpleValidator<String> {
    private List<String> dateFormatPatterns = null;

    public Boolean validate(String s) {
        for (String pattern : dateFormatPatterns) {
            if (s.matches(pattern)) {
                return true;
            }
        }

        return false;
    }
}