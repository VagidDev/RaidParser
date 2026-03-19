package com.unifun.raidparser.parser;

import com.unifun.raidparser.config.DatePatternsConfig;
import com.unifun.raidparser.dto.DateParseResponse;
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
    void parseToLocalDate_SuccessParsingISODateFormat() {
        String isoDate = "2026-03-13";
        DateParseResponse dateParseResponse = dateParser.parseToLocalDate(isoDate);

        assertEquals(LocalDate.parse(isoDate, isoFormatter), dateParseResponse.result());
    }

    @Test
    void parseToLocalDate_FailedParsingISODateFormat_ThirteenMonth() {
        String isoDate = "2026-13-13";
        DateParseResponse dateParseResponse = dateParser.parseToLocalDate(isoDate);

        assertFalse(dateParseResponse.isParsed());
    }

    @Test
    void parseToLocalDate_FailedParsingISODateFormat_ThirtyFebruary() {
        //unexpected, but it is parsed as a 28 February
        String isoDate = "2026-02-30";
        DateParseResponse dateParseResponse = dateParser.parseToLocalDate(isoDate);
        assertTrue(dateParseResponse.isParsed());
    }

    @Test
    void parseToLocalDate_SuccessParsingAmericanDateFormat() {
        String americanDate = "03/13/2026";
        DateParseResponse dateParseResponse = dateParser.parseToLocalDate(americanDate);

        assertEquals(LocalDate.parse(americanDate, americanFormatter), dateParseResponse.result());
    }

    @Test
    void parseToLocalDate_FailedParsingAmericanDateFormat_ThirteenMonth() {
        String isoDate = "13/13/2026";
        DateParseResponse dateParseResponse = dateParser.parseToLocalDate(isoDate);

        assertFalse(dateParseResponse.isParsed());
    }

    @Test
    void parseToLocalDate_SuccessParsingEuropeanDateFormat() {
        String europeDate = "13.03.2026";
        DateParseResponse dateParseResponse = dateParser.parseToLocalDate(europeDate);

        assertEquals(LocalDate.parse(europeDate, europeanFormatter), dateParseResponse.result());
    }

    @Test
    void parseToLocalDate_FailedParsingEuropeanDateFormat_ThirteenMonth() {
        String isoDate = "13.13.2026";
        DateParseResponse dateParseResponse = dateParser.parseToLocalDate(isoDate);

        assertFalse(dateParseResponse.isParsed());
    }

    @Test
    void parseToString_SuccessParsingDateToISOFormat() {
        String isoDate = "2026-03-18";
        LocalDate date = LocalDate.parse(isoDate, isoFormatter);

        String result = dateParser.parseToString(date);

        assertEquals(isoDate.replace("-", "_"), result);
    }

    @Test
    void parseToString_FailedParsingDate_incorrectConfiguration() {
        DatePatternsConfig config = new DatePatternsConfig();
        config.setDateStringFormat("incorrect format");
        DateParser customParser = new DateParser(config);

        String isoDate = "2026-03-18";
        LocalDate date = LocalDate.parse(isoDate, isoFormatter);

        String result = customParser.parseToString(date);

        assertEquals("", result);
    }

}