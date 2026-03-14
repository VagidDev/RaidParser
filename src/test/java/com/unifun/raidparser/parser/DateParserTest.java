package com.unifun.raidparser.parser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DateParserTest {
    private DateParser dateParser;

    @Autowired
    public DateParserTest(DateParser dateParser) {
        this.dateParser = dateParser;
    }

    @Test
    void parse_SuccessParsingISODateFormat() {
        String isoDate = "2026-03-13";
        DateParseResponse dateParseResponse = dateParser.parse(isoDate);

        assertEquals(LocalDate.parse(isoDate, DateTimeFormatter.ISO_DATE), dateParseResponse.result());
    }
}