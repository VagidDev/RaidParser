package com.unifun.raidparser.console;

import com.unifun.raidparser.dto.DateParseResponse;
import com.unifun.raidparser.parser.DateParser;
import com.unifun.raidparser.service.RaidParserService;
import com.unifun.raidparser.service.SftpFileService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class InteractiveConsoleHandler {
    private final static Logger LOGGER = LogManager.getLogger(InteractiveConsoleHandler.class);
    //TODO: Add console logger for user console-interface, maybe get it from AI
    private final RaidParserService raidParserService;
    private final SftpFileService sftpFileService;
    private final DateParser dateParser;

    public void startInteractiveSession() {
        Scanner consoleInput = new Scanner(System.in);
        Path reportFilePath = getReportFileForParsing(consoleInput);
        if (reportFilePath == null) {
            LOGGER.warn("Stop Application due to null report file path");
            System.exit(0);
        }
        commandSession(consoleInput, reportFilePath);
    }

    @Nullable
    private Path getReportFileForParsing(Scanner consoleInput) {
        do {
            LocalDate date = inputDate(consoleInput);
            if (date == null) {
                LOGGER.warn("Received null after getting date for parsing");
                return null;
            }
            Path localFilePath = sftpFileService.getFileForDate(date);
            if (localFilePath == null) {
                System.out.printf("Cannot find report file for date %s. Please try again%n", dateParser.parseToString(date, "yyyy-MM-dd"));
                LOGGER.warn("Cannot get report file for date {}", date);
            } else {
                return localFilePath;
            }
        } while (true);
    }

    private LocalDate inputDate(Scanner consoleInput) {
        String input;
        do {
            System.out.println("Input date of report:");
            input = consoleInput.nextLine();

            if (input.equalsIgnoreCase("stop")) {
                return null;
            }

            if (input.equalsIgnoreCase("today") || input.isBlank()) {
                return LocalDate.now();
            }

            DateParseResponse response = dateParser.parseToLocalDate(input);

            if (response.isParsed()) {
                return response.result();
            } else {
                System.out.printf("Unrecognized date format for input %s. Please try again%n", input);
            }
        } while (true);
    }

    private void commandSession(Scanner consoleInput, Path reportFilePath) {
        do {
            System.out.println("Raid Parser is started!\n" +
                    "Input command:\n" +
                    "parse - parse raid report to txt files\n" +
                    "check - check RAID health of servers manually\n" +
                    "export - export data to google sheets\n" +
                    "stop - stop the application");
            String input = consoleInput.nextLine();
            if (input.equalsIgnoreCase("stop")) {
                LOGGER.warn("Stopping the application");
                System.exit(0);
            }

            switch (input) {
                case "parse" -> {
                    int countDriveStatusParsedServers = raidParserService.writeSortedDriveStatusToFile(reportFilePath);
                    LOGGER.info("Parsed {} servers for drive status from report file {}", countDriveStatusParsedServers, reportFilePath);
                    int countPsuStatusParsedServers = raidParserService.writeSortedPowerSupplyUnitStatusToFile(reportFilePath);
                    LOGGER.info("Parsed {} servers for power supply unit status from report file {}", countPsuStatusParsedServers, reportFilePath);
                    int countBatteryStatusParsedServers = raidParserService.writeSortedBatteryStatusToFile(reportFilePath);
                    LOGGER.info("Parsed {} servers for battery status from report file {}", countBatteryStatusParsedServers, reportFilePath);
                }
            }
        } while (true);
    }
}
