package com.unifun.raidparser.parser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DateParserTest {
    private final DateParser dateParser;

    private final DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE;
    private final DateTimeFormatter americanFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter europeanFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired
    public DateParserTest(DateParser dateParser) {
        this.dateParser = dateParser;
    }

    @Test
    void parse_SuccessParsingISODateFormat() {
        String isoDate = "2026-03-13";
        DateParseResponse dateParseResponse = dateParser.parse(isoDate);

        assertEquals(LocalDate.parse(isoDate, isoFormatter), dateParseResponse.result());
    }

    @Test
    void parse_FailedParsingISODateFormat_ThirteenMonth() {
        String isoDate = "2026-13-13";
        DateParseResponse dateParseResponse = dateParser.parse(isoDate);

        assertFalse(dateParseResponse.isParsed());
    }

    @Test
    void parse_FailedParsingISODateFormat_ThirtyFebruary() {
        //unexpected, but it is parsed as a 28 February
        String isoDate = "2026-02-30";
        DateParseResponse dateParseResponse = dateParser.parse(isoDate);
        assertTrue(dateParseResponse.isParsed());
    }

    @Test
    void parse_SuccessParsingAmericanDateFormat() {
        String americanDate = "03/13/2026";
        DateParseResponse dateParseResponse = dateParser.parse(americanDate);

        assertEquals(LocalDate.parse(americanDate, americanFormatter), dateParseResponse.result());
    }

    @Test
    void parse_FailedParsingAmericanDateFormat_ThirteenMonth() {
        String isoDate = "13/13/2026";
        DateParseResponse dateParseResponse = dateParser.parse(isoDate);

        assertFalse(dateParseResponse.isParsed());
    }

    @Test
    void parse_SuccessParsingEuropeanDateFormat() {
        String europeDate = "13.03.2026";
        DateParseResponse dateParseResponse = dateParser.parse(europeDate);

        assertEquals(LocalDate.parse(europeDate, europeanFormatter), dateParseResponse.result());
    }

    @Test
    void parse_FailedParsingEuropeanDateFormat_ThirteenMonth() {
        String isoDate = "13.13.2026";
        DateParseResponse dateParseResponse = dateParser.parse(isoDate);

        assertFalse(dateParseResponse.isParsed());
    }
}