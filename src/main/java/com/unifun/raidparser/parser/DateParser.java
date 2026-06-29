package com.unifun.raidparser.parser;

import com.unifun.raidparser.config.DatePatternsConfig;
import com.unifun.raidparser.dto.DateParseResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DateParser {
    private static final Logger LOGGER = LogManager.getLogger(DateParser.class);
    private final DatePatternsConfig datePatternsConfig;

    public String parseToString(LocalDate date, String format) {
        try {
            DateTimeFormatter toStringDateFormatter = DateTimeFormatter.ofPattern(format);
            return date.format(toStringDateFormatter);
        } catch (Exception e) {
            LOGGER.error(
                    "Cannot format date `{}` to pattern `{}`. Catch exception -> {}",
                    date,
                    format,
                    e.getLocalizedMessage(),
                    e
            );
            return "";
        }
    }

    public DateParseResponse parseToLocalDate(String stringDate) {
        for (Map.Entry<String, String> patternEntry : datePatternsConfig.getFormats().entrySet()) {
            if (stringDate.matches(patternEntry.getValue())) {
                LOGGER.info(
                        "Date `{}` is valid for regex `{}`. Date will be parsed with format `{}`",
                        stringDate,
                        patternEntry.getValue(),
                        patternEntry.getKey()
                );
                return buildSuccessResponse(stringDate, patternEntry.getKey());
            }
        }
        LOGGER.info("Cannot parse the date `{}`", stringDate);
        return new DateParseResponse(false, LocalDate.now());
    }

    private DateParseResponse buildSuccessResponse(String stringDate, String dateFormat) {
        LocalDate date = LocalDate.parse(stringDate, DateTimeFormatter.ofPattern(dateFormat));
        return new DateParseResponse(true, date);
    }
}
