package com.unifun.raidparser;

import com.unifun.raidparser.loader.SftpFileLoader;
import com.unifun.raidparser.config.AppConfig;
import com.unifun.raidparser.service.RaidParserService;
//import com.unifun.raidparser.sheets.GoogleSheetsExporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main {
    public static final Logger CONSOLE_LOGGER = LogManager.getLogger("com.unifun.console_log");
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");

    private static final String raidParserMenu = "Raid Parser is started!\nInput command: \n" +
            "parse - parse raid report to txt files\n" +
            "check - check RAID health of servers manually\n" +
            "export - export data to google sheets\n" +
            "stop - stop the application";


    public static void not(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String raidReportPath = "";

        LOGGER.info("Starting application...");

        //menu for choosing raid report for parsing
        System.out.println("Please input data of raid report (yyyy_MM_dd) or `stop` for exit application:");

        do {
            String dateInput = scanner.nextLine();
            LOGGER.info("User input -> `{}`", dateInput);

            if (dateInput.equals("stop")) {
                LOGGER.info("Stopping application user input for date");
                System.exit(0);
            }

            //if empty - get current date
            if (dateInput.isBlank()) {
                dateInput = simpleDateFormat.format(new Date());
                LOGGER.info("Input date is empty, get today date: {} ", dateInput);
            }

            raidReportPath = "";//SftpFileLoader.getFileForDate(dateInput);

            if (raidReportPath.isBlank()) {
                CONSOLE_LOGGER.warn("Got empty file for parsing. Please see logs for more information");
            }
        } while (raidReportPath.isBlank());

        System.out.println(raidParserMenu);

        String input = "";
        do {
            input = scanner.nextLine().trim();

            switch (input) {
                case "parse":
                    parse(raidReportPath);
                    break;
                case "export":
                    CONSOLE_LOGGER.info("Start exporting to Google Sheets");
//                    GoogleSheetsExporter.export(raidReportPath);
                    CONSOLE_LOGGER.info("Finish exporting data!");
                    break;
                case "stop":
                    System.out.println("Goodbye :(");
                    LOGGER.info("Stopping application at menu stage");
                    break;
                default:
                    CONSOLE_LOGGER.info("{} -> Unknown command!", input);
            }
        } while (!input.equals("stop"));
    }

    private static void parse(String raidReportPath) {
        final String defaultStatusDir = "./status/";
        final String defaultDriveStatusFile = "driver_status.txt";
        final String defaultPSUStatusFile = "psu_status.txt";
        final String defaultBatteryStatusFile = "battery_status.txt";

        RaidParserService service = new RaidParserService();
        CONSOLE_LOGGER.info("Start parsing data from {} ", raidReportPath);

        String driveOutputPath = AppConfig.get("file.output.driver").isBlank() ? defaultStatusDir + defaultDriveStatusFile : AppConfig.get("file.output.driver");
        String psuOutputPath = AppConfig.get("file.output.supply").isBlank() ? defaultStatusDir + defaultPSUStatusFile : AppConfig.get("file.output.supply");
        String batteryOutputPath = AppConfig.get("file.output.battery").isBlank() ? defaultStatusDir + defaultBatteryStatusFile : AppConfig.get("file.output.battery");

        service.getAllRaidStatusAndWriteToFiles(raidReportPath, driveOutputPath, batteryOutputPath, psuOutputPath);
        CONSOLE_LOGGER.info("Finish parsing data from {}. Status were exported to:\n{}\n{}\n{}", raidReportPath, driveOutputPath, psuOutputPath, batteryOutputPath);
    }


}