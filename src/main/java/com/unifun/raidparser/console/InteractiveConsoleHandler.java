package com.unifun.raidparser.console;

import com.unifun.raidparser.dto.DateParseResponse;
import com.unifun.raidparser.parser.DateParser;
import com.unifun.raidparser.service.RaidParserService;
import com.unifun.raidparser.service.SftpFileService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Scanner;

@Component
@RequiredArgsConstructor
public class InteractiveConsoleHandler {
    private final static Logger LOGGER = LogManager.getLogger(InteractiveConsoleHandler.class);

    private final RaidParserService raidParserService;
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
            System.out.println(" [3] export - Экспорт в Google Sheets");
            System.out.println(" [back]     - Выбрать другой файл/дату");
            System.out.println(" [exit]     - Выйти из программы");
            System.out.print("> ");

            String input = consoleInput.nextLine().trim().toLowerCase();

            switch (input) {
                case "1", "parse" -> executeParsing(reportFilePath);
                case "2", "check" -> printMsg("Функция 'check' в разработке...");
                case "3", "export" -> printMsg("Функция 'export' в разработке...");
                case "back" -> { return false; }
                case "exit", "stop" -> System.exit(0);
                default -> printError("Неизвестная команда. Попробуйте еще раз.");
            }
        }
    }

    private void executeParsing(Path reportFilePath) {
        printMsg("Запуск процесса парсинга...");

        try {
            LOGGER.warn("Not implemented yet");
            int drives = 0;//raidParserService.writeSortedDriveStatusToFile(reportFilePath);
            int psu = 0;//raidParserService.writeSortedPowerSupplyUnitStatusToFile(reportFilePath);
            int battery = 0;//raidParserService.writeSortedBatteryStatusToFile(reportFilePath);

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

    // Вспомогательные методы для красоты
    private void printHeader() {
        System.out.println("\n" + LOGO);
        System.out.println("             System Administration Tool v1.0");
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