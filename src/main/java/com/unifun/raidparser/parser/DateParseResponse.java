package com.unifun.raidparser.parser;

import java.time.LocalDate;

public record DateParseResponse (
        boolean isParsed,
        LocalDate result
) implements ParseResponse<LocalDate> {}
