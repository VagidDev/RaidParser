package com.unifun.raidparser.dto;

import java.time.LocalDate;

public record DateParseResponse (
        boolean isParsed,
        LocalDate result
) implements ParseResponse<LocalDate> {}
