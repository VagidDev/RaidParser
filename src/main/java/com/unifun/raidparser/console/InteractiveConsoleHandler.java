package com.unifun.raidparser.console;

import com.unifun.raidparser.config.OutputStatusFileConfig;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.filters.driver.DriverStatus;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.dto.DateParseResponse;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.exporter.FileExporter;
import com.unifun.raidparser.exporter.GoogleSheetExporter;
import com.unifun.raidparser.handlers.ParsedRaidStatusDataHandler;
import com.unifun.raidparser.parser.DateParser;
import com.unifun.raidparser.service.RaidParserService;
import com.unifun.raidparser.service.SftpFileService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class InteractiveConsoleHandler {
    private final static Logger LOGGER = LogManager.getLogger(InteractiveConsoleHandler.class);
    //Configs
    private final OutputStatusFileConfig outputStatusFileConfig;

    //Services
    private final ParsedRaidStatusDataHandler parsedRaidStatusDataHandler;
    private final FileExporter fileExporter;
    private final GoogleSheetExporter googleSheetExporter;
    private final SftpFileService sftpFileService;
    private final DateParser dateParser;

    // Константы для оформления
    private static final String SEPARATOR = "====================================================";
    private static final String LOGO =
            "  ____       _     _   ____                                \n"
                    + " |  _ \\ __ _(_) __| | |  _ \\ __ _ _ __ ___  ___ _ __      \n"
                    + " | |_) / _` | |/ _` | | |_) / _` | '__/ __|/ _ \\ '__|     \n"
                    + " |  _ < (_| | | (_| | |  __/ (_| | |  \\__ \\  __/ |        \n"
                    + " |_| \\_\\__,_|_|\\__,_| |_|   \\__,_|_|  |___/\\___|_|        ";

    public void startInteractiveSession() {
        Scanner consoleInput = new Scanner(System.in);
        printHeader();

        while (true) {
            Path reportFilePath = getReportFileForParsing(consoleInput);

            if (reportFilePath == null) {
                printMsg("Завершение работы... До встречи!");
                System.exit(0);
            }

            boolean continueWithSameFile = commandSession(consoleInput, reportFilePath);
            if (!continueWithSameFile) {
                printMsg("Возврат к выбору даты...");
            }
        }
    }

    private Path getReportFileForParsing(Scanner consoleInput) {
        while (true) {
            System.out.println("\n" + SEPARATOR);
            printMsg("ШАГ 1: ВЫБОР ОТЧЕТА");
            System.out.println("Введите дату отчета (гггг-мм-дд), 'today' или 'exit' для выхода:");
            System.out.print("> ");

            String input = consoleInput.nextLine().trim();

            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("stop")) {
                return null;
            }

            LocalDate date;
            if (input.isEmpty() || input.equalsIgnoreCase("today")) {
                date = LocalDate.now();
            } else {
                DateParseResponse response = dateParser.parseToLocalDate(input);
                if (!response.isParsed()) {
                    printError("Неверный формат даты: " + input);
                    continue;
                }
                date = response.result();
            }

            printMsg("Поиск файла на SFTP для даты: " + dateParser.parseToString(date, "yyyy-MM-dd") + "...");
            Path localFilePath = sftpFileService.getFileForDate(date);

            if (localFilePath != null) {
                printMsg("Файл успешно получен: " + localFilePath.getFileName());
                return localFilePath;
            } else {
                printError("Файл для указанной даты не найден на сервере.");
            }
        }
    }

    private boolean commandSession(Scanner consoleInput, Path reportFilePath) {
        while (true) {
            System.out.println("\n" + SEPARATOR);
            printMsg("ШАГ 2: ДЕЙСТВИЯ (Файл: " + reportFilePath.getFileName() + ")");
            System.out.println("доступные команды:");
            System.out.println(" [1] parse  - Парсинг отчета (Drive, PSU, Battery)");
            System.out.println(" [2] check  - Ручная проверка RAID Health");
            System.out.println(" [3] file-export - Экспорт в статус-файлы");
            System.out.println(" [4] sheets-export - Экспорт в Google Sheets");
            System.out.println(" [back]     - Выбрать другой файл/дату");
            System.out.println(" [exit]     - Выйти из программы");
            System.out.print("> ");

            String input = consoleInput.nextLine().trim().toLowerCase();

            switch (input) {
                case "1", "parse" -> executeParsing(reportFilePath);
                case "2", "check" -> executeChecking(reportFilePath);
                case "3", "file-export" -> exportToFile(reportFilePath);
                case "4", "sheets-export" -> exportToGoogleSheets(reportFilePath);
                case "back" -> { return false; }
                case "exit", "stop" -> System.exit(0);
                default -> printError("Неизвестная команда. Попробуйте еще раз.");
            }
        }
    }

    private void executeParsing(Path reportFilePath) {
        printMsg("Запуск процесса парсинга...");

        try {
            int drives = parsedRaidStatusDataHandler.getSortedDriveStatus(reportFilePath).size();
            int psu = parsedRaidStatusDataHandler.getSortedPowerSupplyStatus(reportFilePath).size();
            int battery = parsedRaidStatusDataHandler.getSortedBatteryStatus(reportFilePath).size();

            System.out.println("----------------------------------------------------");
            printMsg("РЕЗУЛЬТАТЫ ПАРСИНГА:");
            System.out.printf(" - Диски (Drive Status): %d серверов%n", drives);
            System.out.printf(" - Блоки питания (PSU):  %d серверов%n", psu);
            System.out.printf(" - Батареи (Battery):    %d серверов%n", battery);
            System.out.println("----------------------------------------------------");

            LOGGER.info("Successfully parsed report {}. D:{}, P:{}, B:{}", reportFilePath, drives, psu, battery);
        } catch (Exception e) {
            printError("Ошибка при парсинге: " + e.getMessage());
            LOGGER.error("Parsing error", e);
        }
    }

    private void executeChecking(Path reportFilePath) {
        printMsg("Запуск процесса сбора полного статуса дисков...");
        List<ServerStatus<DriverStatus>> fullDriveStatus = parsedRaidStatusDataHandler.getSortedFullDriveStatus(reportFilePath);
        printMsg("Печатаю текущий статус ниже:");

        fullDriveStatus.forEach(serverStatus -> printMsg(
                        String.format(
                                "Сервер: %s -> Статус: %s -> Текст статуса %s",
                                serverStatus.serverName(),
                                serverStatus.analyzeResponse().getStatus().getName(),
                                serverStatus.analyzeResponse().getErrorText()
                        )
                )
        );
        printMsg("Статус собран!");
    }

    private void exportToFile(Path reportFilePath) {
        printMsg("Запуск процесса экспорта в файл...");

        Path driveFileStatusPath = Path.of(outputStatusFileConfig.getDriveStatus());
        Path powerSupplyFileStatusPath = Path.of(outputStatusFileConfig.getPsuStatus());
        Path batteryFileStatusPath = Path.of(outputStatusFileConfig.getBatteryStatus());

        fileExporter.export(driveFileStatusPath, parsedRaidStatusDataHandler.getSortedDriveStatus(reportFilePath));
        fileExporter.export(powerSupplyFileStatusPath, parsedRaidStatusDataHandler.getSortedPowerSupplyStatus(reportFilePath));
        fileExporter.export(batteryFileStatusPath, parsedRaidStatusDataHandler.getSortedBatteryStatus(reportFilePath));

        printMsg(String.format("Данные успешно экспортированы в файлы: %s | %s | %s", driveFileStatusPath, powerSupplyFileStatusPath, batteryFileStatusPath));
    }

    private void exportToGoogleSheets(Path reportFilePath) {
        printMsg("Запуск процесса экспорта в Google Sheets...");

        googleSheetExporter.export(parsedRaidStatusDataHandler.getSortedDriveStatus(reportFilePath), DriverStatus.class);
        googleSheetExporter.export(parsedRaidStatusDataHandler.getSortedPowerSupplyStatus(reportFilePath), PowerSupplyStatus.class);
        googleSheetExporter.export(parsedRaidStatusDataHandler.getSortedBatteryStatus(reportFilePath), BatteryStatus.class);

        printMsg("Данные успешно экспортированы в Google Sheets!");
    }

    // Вспомогательные методы для красоты
    private void printHeader() {
        System.out.println("\n" + LOGO);
        System.out.println("             System Administration Tool v4.0");
        System.out.println(SEPARATOR);
    }

    private void printMsg(String msg) {
        //[INFO]
        System.out.println("\u001B[32m[INFO]\u001B[0m " + msg);
    }

    private void printError(String error) {
        System.err.println("[ERROR] " + error);
        // Небольшая задержка, чтобы err не перемешивался с out в консоли IDE
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
    }
}