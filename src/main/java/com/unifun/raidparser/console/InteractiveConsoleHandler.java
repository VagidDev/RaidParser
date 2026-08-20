package com.unifun.raidparser.console;

import com.unifun.raidparser.config.OutputStatusFileConfig;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.DateParseResponse;
import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.mapper.ExportDataMapper;
import com.unifun.raidparser.exporter.FileExporter;
import com.unifun.raidparser.exporter.GoogleSheetExporter;
import com.unifun.raidparser.parser.DateParser;
import com.unifun.raidparser.service.RaidParserService;
import com.unifun.raidparser.service.ServerHealthCheckService;
import com.unifun.raidparser.service.SftpFileService;
import com.unifun.raidparser.util.ServerStatusSorter;
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
    private final GoogleSheetExporter googleSheetExporter;
    private final ServerStatusSorter serverStatusSorter;
    private final RaidParserService raidParserService;
    private final ExportDataMapper exportDataMapper;
    private final SftpFileService sftpFileService;
    private final FileExporter fileExporter;
    private final DateParser dateParser;
    //temporary
    private final ServerHealthCheckService serverHealthCheckService;

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
            System.out.println(" [1] parse-report - Парсинг отчета и вывод в консоль");
            System.out.println(" [2] check-health - Проверка состояния в ручную командами и вывод в консоль");
            System.out.println(" [3] full-check - Парсинг отчета и проверка состояния - вывод в консоль");
            System.out.println(" [4] file-export - Экспорт статуса из кэша в статус-файлы");
            System.out.println(" [5] sheets-export - Экспорт статуса из кэша в Google Sheets");
            System.out.println(" [8] print-cache - Показать кэш");
            System.out.println(" [9] clear-cache - Очистить кэш");
            System.out.println(" [back]     - Выбрать другой файл/дату");
            System.out.println(" [exit]     - Выйти из программы");
            System.out.print("> ");

            String input = consoleInput.nextLine().trim().toLowerCase();

            switch (input) {
                case "1", "parse-report" -> executeParsing(reportFilePath);
                case "2", "check-health" -> executeHardDriveChecking();
                case "3", "full-check" -> {
                    raidParserService.clearCache();
                    raidParserService.analyzeStatusFromReportFile(reportFilePath);
                    raidParserService.analyzeStatusFromHosts();
                    printStatus(raidParserService.getCachedStatus());
                }
                case "4", "file-export" -> exportToFile();
                case "5", "sheets-export" -> exportToGoogleSheets();
                case "8", "print-cache" -> printStatus(raidParserService.getCachedStatus());
                case "9", "clear-cache" -> raidParserService.clearCache();
                case "back" -> { return false; }
                case "exit", "stop" -> System.exit(0);
                default -> printError("Неизвестная команда. Попробуйте еще раз.");
            }
        }
    }

    private void executeParsing(Path reportFilePath) {
        printMsg(String.format("Запуск процесса парсинга статуса из файла %s ...", reportFilePath.toString()));
        List<ServerStatus> serverStatuses = raidParserService.analyzeStatusFromReportFile(reportFilePath);
        printMsg("Распарсил " + serverStatuses.size() + " серверов");
        printStatus(serverStatuses);
    }

    private void printStatus(List<ServerStatus> serverStatuses) {
        printMsg("Вывод статуса в консоль...");
        List<ReportServerData> driveStatus = exportDataMapper.map(
                serverStatusSorter.sortByHealthStatus(serverStatuses, HealthType.DRIVE_HEALTH),
                HealthType.DRIVE_HEALTH
        );
        List<ReportServerData> psuStatus = exportDataMapper.map(
                serverStatusSorter.sortByHealthStatus(serverStatuses, HealthType.PSU_HEALTH),
                HealthType.PSU_HEALTH
        );
        List<ReportServerData> batteryStatus = exportDataMapper.map(
                serverStatusSorter.sortByHealthStatus(serverStatuses, HealthType.BATTERY_HEALTH),
                HealthType.BATTERY_HEALTH
        );

        System.out.println("----------------------------------------------------");
        printMsg("РЕЗУЛЬТАТЫ ПАРСИНГА:");
        System.out.printf(" - Диски (Drive Status): %d серверов%n", driveStatus.size());
        System.out.printf(" - Блоки питания (PSU):  %d серверов%n", psuStatus.size());
        System.out.printf(" - Батареи (Battery):    %d серверов%n", batteryStatus.size());
        System.out.println("----------------------------------------------------");

        printMsg("ВЫВОД:");
        printMsg("Статус дисков:");
        driveStatus.forEach(serverStatus -> printMsg(serverStatus.getPrettyFormat()));
        printMsg("Статус Блоков питания:");
        psuStatus.forEach(serverStatus -> printMsg(serverStatus.getPrettyFormat()));
        printMsg("Статус батареек:");
        batteryStatus.forEach(serverStatus -> printMsg(serverStatus.getPrettyFormat()));

        LOGGER.info("Successfully parsed report D:{}, P:{}, B:{}", driveStatus.size(), psuStatus.size(), batteryStatus.size());
    }

    private void executeHardDriveChecking() {
        printMsg("Запуск процесса проверки статуса на серверах ...");
        List<ServerStatus> serverData = serverStatusSorter.sortByHealthStatus(
                raidParserService.analyzeStatusFromHosts(),
                HealthType.DRIVE_HEALTH
        );
        printMsg("Печатаю текущий статус ниже:");

        exportDataMapper.map(serverData, HealthType.DRIVE_HEALTH).forEach(reportServerData -> printMsg(
                        String.format(
                                "Сервер: %s -> Статус: %s -> Текст статуса %s",
                                reportServerData.serverName(),
                                reportServerData.healthStatus(),
                                reportServerData.errorText()
                        )
                )
        );
        printMsg("Процесс проверки статуса выполнен выполнен!");
    }

    private void exportToFile() {
        printMsg("Запуск процесса экспорта статусов из кэша в файл...");

        Path driveFileStatusPath = Path.of(outputStatusFileConfig.getDriveStatus());
        Path powerSupplyFileStatusPath = Path.of(outputStatusFileConfig.getPsuStatus());
        Path batteryFileStatusPath = Path.of(outputStatusFileConfig.getBatteryStatus());

        List<ServerStatus> serverStatusList = raidParserService.getCachedStatus();

        fileExporter.export(driveFileStatusPath, exportDataMapper.map(
                serverStatusSorter.sortByHealthStatus(
                        serverStatusList,
                        HealthType.DRIVE_HEALTH
                ),
                HealthType.DRIVE_HEALTH
        ));
        fileExporter.export(powerSupplyFileStatusPath, exportDataMapper.map(
                serverStatusSorter.sortByHealthStatus(
                        serverStatusList,
                        HealthType.PSU_HEALTH
                ),
                HealthType.PSU_HEALTH
        ));

        fileExporter.export(batteryFileStatusPath, exportDataMapper.map(
                serverStatusSorter.sortByHealthStatus(
                        serverStatusList,
                        HealthType.BATTERY_HEALTH
                ),
                HealthType.BATTERY_HEALTH
        ));

        printMsg(String.format("Данные успешно экспортированы в файлы: %s | %s | %s", driveFileStatusPath, powerSupplyFileStatusPath, batteryFileStatusPath));
    }

    private void exportToGoogleSheets() {
        printMsg("Запуск процесса экспорта статуса из кэша в Google Sheets...");

        List<ServerStatus> serverStatusList = raidParserService.getCachedStatus();

        googleSheetExporter.export(exportDataMapper.map(
                serverStatusSorter.sortByHealthStatus(
                        serverStatusList,
                        HealthType.DRIVE_HEALTH
                ),
                HealthType.DRIVE_HEALTH
        ), HealthType.DRIVE_HEALTH);

        googleSheetExporter.export(exportDataMapper.map(
                serverStatusSorter.sortByHealthStatus(
                        serverStatusList,
                        HealthType.PSU_HEALTH
                ),
                HealthType.PSU_HEALTH
        ), HealthType.PSU_HEALTH);

        googleSheetExporter.export(exportDataMapper.map(
                serverStatusSorter.sortByHealthStatus(
                        serverStatusList,
                        HealthType.BATTERY_HEALTH
                ),
                HealthType.BATTERY_HEALTH
        ), HealthType.BATTERY_HEALTH);

        printMsg("Данные успешно экспортированы в Google Sheets!");
    }

    // Вспомогательные методы для красоты
    private void printHeader() {
        System.out.println("\n" + LOGO);
        System.out.println("             System Administration Tool v5.0");
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