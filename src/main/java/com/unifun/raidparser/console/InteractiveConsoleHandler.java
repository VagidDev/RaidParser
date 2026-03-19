package com.unifun.raidparser.console;

import com.unifun.raidparser.dto.DateParseResponse;
import com.unifun.raidparser.parser.DateParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class InteractiveConsoleHandler {
    private final DateParser dateParser;

    public void startInteractiveSession() {
        Scanner consoleInput = new Scanner(System.in);
        LocalDate localDate = inputDate(consoleInput);

    }

    private LocalDate inputDate(Scanner consoleInput) {
        String input;
        do {
            input = consoleInput.nextLine();

            if (input.equalsIgnoreCase("today") || input.isBlank()) {
                return LocalDate.now();
            }

            DateParseResponse response = dateParser.parseToLocalDate(input);

            if (response.isParsed()) {
                return response.result();
            }

        } while (!input.equalsIgnoreCase("stop"));

        return null;
    }
}
