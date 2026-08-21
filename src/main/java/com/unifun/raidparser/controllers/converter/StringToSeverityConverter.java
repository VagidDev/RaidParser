package com.unifun.raidparser.controllers.converter;

import com.unifun.raidparser.core.component.Severity;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

/** Принимает severity в любом регистре: CRITICAL, critical, no-data. */
@Component
public class StringToSeverityConverter implements Converter<String, Severity> {

    @Override
    public Severity convert(String source) {
        String normalized = source.trim().toUpperCase(Locale.ROOT).replace('-', '_');

        return Arrays.stream(Severity.values())
                .filter(severity -> severity.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown severity `" + source + "`. Expected one of: " + Arrays.toString(Severity.values())));
    }
}
