package com.unifun.raidparser.console;

import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.dto.DateParseResponse;
import com.unifun.raidparser.dto.ReportServerData;
import com.unifun.raidparser.dto.HostInformation;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.handlers.CacheState;
import com.unifun.raidparser.mapper.ExportDataMapper;
import com.unifun.raidparser.parser.DateParser;
import com.unifun.raidparser.service.CacheService;
import com.unifun.raidparser.service.HostOverviewService;
import com.unifun.raidparser.service.RaidParserService;
import com.unifun.raidparser.service.StatusExportService;
import com.unifun.raidparser.service.SftpFileService;
import com.unifun.raidparser.util.ServerStatusSorter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@Component
@Profile(Profiles.CONSOLE)
@RequiredArgsConstructor
public class InteractiveConsoleHandler {
    private final static Logger LOGGER = LogManager.getLogger(InteractiveConsoleHandler.class);
    //Services
    private final StatusExportService statusExportService;
    private final ServerStatusSorter serverStatusSorter;
    private final RaidParserService raidParserService;
    private final ExportDataMapper exportDataMapper;
    private final SftpFileService sftpFileService;
    private final HostOverviewService hostOverviewService;
    private final CacheService cacheService;
    private final DateParser dateParser;
    private static final boolean EXIT_SESSION = true;
    private static final boolean BACK_TO_FILE_CHOICE = false;

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
                return;
            }

            if (commandSession(consoleInput, reportFilePath) == EXIT_SESSION) {
                printMsg("Завершение работы... До встречи!");
                return;
            }
            printMsg("Возврат к выбору даты...");
        }
    }

    private Path getReportFileForParsing(Scanner consoleInput) {
        while (true) {
            System.out.println("\n" + SEPARATOR);
            printMsg("ШАГ 1: ВЫБОР ОТЧЕТА");
            System.out.println("Введите дату отчета (гггг-мм-дд), 'today' или 'exit' для выхода:");
            System.out.print("> ");

            if (!consoleInput.hasNextLine()) {
                // ввод закончился (Ctrl+D или запуск через pipe) — это тоже выход
                return null;
            }
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

    /**
     * @return {@link #EXIT_SESSION}, если нужно выйти из приложения,
     *         {@link #BACK_TO_FILE_CHOICE} — если вернуться к выбору отчёта.
     */
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
            System.out.println(" [6] refresh-hosts - Перечитать список серверов из HostOverview");
            System.out.println(" [7] cache-info - Состояние кэшей: размер, возраст, TTL");
            System.out.println(" [8] print-cache - Показать статусы из кэша");
            System.out.println(" [9] clear-cache [имя] - Очистить кэш, без имени - все: " + String.join(", ", cacheService.cacheNames()));
            System.out.println(" [back]     - Выбрать другой файл/дату");
            System.out.println(" [exit]     - Выйти из программы");
            System.out.print("> ");

            if (!consoleInput.hasNextLine()) {
                return EXIT_SESSION;
            }
            String input = consoleInput.nextLine().trim().toLowerCase();
            String[] inputParts = input.split("\\s+");
            String command = inputParts[0];

            switch (command) {
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
                case "6", "refresh-hosts" -> refreshHosts();
                case "7", "cache-info" -> printCacheInfo();
                case "8", "print-cache" -> printStatus(raidParserService.getCachedStatus());
                case "9", "clear-cache" -> clearCache(inputParts.length > 1 ? inputParts[1] : null);
                case "back" -> { return BACK_TO_FILE_CHOICE; }
                case "exit", "stop" -> { return EXIT_SESSION; }
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

        Map<HealthType, Path> files = statusExportService.exportToFiles();

        printMsg("Данные успешно экспортированы в файлы:");
        files.forEach((healthType, path) -> printMsg(String.format("  %s -> %s", healthType, path)));
    }

    private void exportToGoogleSheets() {
        printMsg("Запуск процесса экспорта статуса из кэша в Google Sheets...");

        Map<HealthType, Boolean> exported = statusExportService.exportToSheets();

        if (exported.values().stream().allMatch(Boolean::booleanValue)) {
            printMsg("Данные успешно экспортированы в Google Sheets!");
            return;
        }
        printError("Экспорт в Google Sheets не выполнен полностью — подробности в логе:");
        exported.forEach((healthType, success) -> printMsg(String.format("  %s -> %s", healthType, success ? "ок" : "не выгружено")));
    }

    private void refreshHosts() {
        printMsg("Перечитываю список серверов из HostOverview...");
        List<HostInformation> servers = hostOverviewService.getActualServers();
        printMsg(String.format("Получено серверов: %d", servers.size()));
    }

    private void printCacheInfo() {
        printMsg("Состояние кэшей:");
        for (CacheState state : cacheService.states()) {
            printMsg("  " + state.describe());
        }
    }

    private void clearCache(String cacheName) {
        if (cacheName == null) {
            cacheService.clearAll();
            printMsg("Все кэши очищены: " + String.join(", ", cacheService.cacheNames()));
            return;
        }

        if (cacheService.clear(cacheName)) {
            printMsg(String.format("Кэш `%s` очищен", cacheName));
        } else {
            printError(String.format("Неизвестный кэш `%s`. Доступны: %s", cacheName, String.join(", ", cacheService.cacheNames())));
        }
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