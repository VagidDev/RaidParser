package com.unifun.raidparser.console;

import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
public class InteractiveConsoleHandler {
    public void startInteractiveSession() {
        Scanner consoleInput = new Scanner(System.in);

    }

    private String inputDate(Scanner consoleInput) {
        String input;
        boolean isCorrectDateFormat = false;
        do {
            input = consoleInput.nextLine();
            //TODO: date format checker
            //return "";
        } while (!input.equals("stop") || isCorrectDateFormat);

        return "";
    }
}
