package com.unifun.raidparser.config;

import com.unifun.raidparser.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

public class AppConfig {
    private static final Logger LOGGER = LogManager.getLogger(AppConfig.class);
    private static final Map<String, String> configuration = new HashMap<>();
    private static final Path configurationPath = Path.of("./raid-parser.conf");
    static {
        boolean isExists = Files.exists(configurationPath);
        if (isExists) {
            try {
                List<String> lines = Files.readAllLines(configurationPath)
                        .stream()
                        .filter(str -> !str.isBlank() && !str.startsWith("#") && str.contains("="))
                        .toList();

                for (String line : lines) {
                    if (line.contains("#"))
                        line = line.chars()
                            .takeWhile(ch -> ch != '#')
                            .mapToObj(ch -> (char) ch)
                            .collect(Collector.of(
                                    StringBuilder::new,
                                    StringBuilder::append,
                                    StringBuilder::append,
                                    StringBuilder::toString
                            ));

                    String[] properties = line.split("=", 2);
                    configuration.put(properties[0].trim(), properties[1].trim());
                }
            } catch (IOException e) {
                LOGGER.error("Error while reading configuration file. Error -> {} ", e.getMessage(), e);
                System.exit(0);
            }
        } else {
            LOGGER.error("Configuration file didn't exist!");
            createDefaultConfiguration();
        }
    }

    public static String get(String key) {
        String value = configuration.get(key);

        if (value == null)
            return "";

        return configuration.get(key);
    }

    private static void createDefaultConfiguration() {
        LOGGER.warn("Creating default configuration file -> {}", configurationPath);
        String defaultConfig =
                "# Raid Parser Configuration\n" +
                        "# Please set up value for required keys below (without this configuration application won't work)\n\n" +

                        "# Configuration for downloading raid report file from sftp\n" +
                        "sftp.host=\n" +
                        "sftp.user=\n" +
                        "sftp.password=\n" +
                        "# Not mandatory values\n" +
                        "# Finds file on sftp server with template `sftp.remote-file-template` + `YYYY_mm_dd`, default `/servers_raid_status_`\n" +
                        "# sftp.remote-file-template=\n" +
                        "# Local directory to save downloaded reports (by default `./raid_reports/`)\n" +
                        "# sftp.local-path-raid-status=\n\n" +

                        "# Configuration for exporting data to google-sheet\n" +
                        "# Credentials from google in json format\n" +
                        "sheets.credentials=\n" +
                        "# Directory, where will be stored token for authentification\n" +
                        "sheets.token-path=\n" +
                        "# Id of google-sheet\n" +
                        "sheets.spreadsheetId=\n" +
                        "# Range in google-sheet for disk-report\n" +
                        "sheets.spreadsheet.disk-range=\n" +
                        "# Range in google-sheet for psu-report\n" +
                        "sheets.spreadsheet.psu-range=\n" +
                        "# Range in google-sheet for battery-report\n" +
                        "sheets.spreadsheet.battery-range=\n\n" +

                        "# Configuration for loading information about servers from HostOverview\n" +
                        "# Username/Login for site HostOverview\n" +
                        "html.user=\n" +
                        "# Password for site HostOverview\n" +
                        "html.password=\n" +
                        "# I don't why I extract this data from hard code, but I did it so, please set up this values\n" +
                        "# Link to login page of HostOverview (In our case https://hostoverview.unifun.com/login.php)\n" +
                        "html.authorization=\n" +
                        "# Link to API that returns information about servers in HTML view (In our case https://hostoverview.unifun.com/fetch.php)\n" +
                        "html.data-loader=\n\n" +

                        "# Configuration for manually checking raid status\n" +
                        "# SSH username\n" +
                        "ssh.user.user=\n" +
                        "# Path to OpenVPN key\n" +
                        "ssh.key=\n" +
                        "# Remote host (in our case IP address of proxy `172.16.197.148`)\n" +
                        "health.proxy=\n" +
                        "# File with format `server_name [->] commandToExecute`\n" +
                        "health.servers-file=\n" +
                        "# Not mandatory values\n" +
                        "# Path to directory where will be saved status for manual checked servers (by default `./server-health/`)\n" +
                        "# manual.report-dir=\n\n" +

                        "# Not mandatory values\n" +
                        "# Configuration for files with parsed RAID status\n" +
                        "# By default `./status/driver_status.txt`\n" +
                        "# file.output.driver=\n" +
                        "# By default `./status/psu_status.txt`\n" +
                        "# file.output.supply=\n" +
                        "# By default `./status/battery_status.txt`\n" +
                        "# file.output.battery=\n"
                ;

        try {
            Files.createFile(configurationPath);
            Files.writeString(configurationPath, defaultConfig);
            Main.CONSOLE_LOGGER.warn("Configuration file created! Please, set up configuration in file `{}` and re-run parser", configurationPath);
            System.exit(0);
        } catch (IOException e) {
            LOGGER.error("Error while creating/writing default configuration file. Error -> {} ", e.getMessage(), e);
            System.exit(0);
        }
    }
}