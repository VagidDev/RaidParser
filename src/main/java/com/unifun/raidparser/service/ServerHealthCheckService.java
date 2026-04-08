package com.unifun.raidparser.service;

import com.unifun.raidparser.config.AppConfig;
import com.unifun.raidparser.dto.ServerInfo;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerHealthCheckService {
    private static final Logger LOGGER = LogManager.getLogger(ServerHealthCheckService.class);

    /**
     * Reading file with host names and command to execute
     * @return Map with server name as key and command to execute as value
     */
    private static Map<String, String> readServersToCheckFromFile() {
        String filePath = AppConfig.get("health.servers-file");
        Map<String, String> serverNameWithCommand = new HashMap<>();
        if (filePath.isEmpty()) {
            LOGGER.error("Please set up configuration for file with servers name and command to execute. Current value for `health.servers-file` -> {}", filePath);
            return serverNameWithCommand;
        }

        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            Scanner scanner = new Scanner(fileInputStream);

            while (scanner.hasNext()) {
                String lineToParse = scanner.nextLine();
                String[] splitLine = lineToParse.split("->");
                if (splitLine.length == 2) {
                    String serverName = splitLine[0].trim();
                    String commandToExecute = splitLine[1].trim();
                    serverNameWithCommand.put(serverName, commandToExecute);
                } else {
                    LOGGER.warn("Cannot get server name and command from line. Line to parse -> {}", lineToParse);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Unexpected error while reading file -> {}. Error -> {}", filePath, e.getMessage(), e);
        }

        return serverNameWithCommand;
    }

    /**
     * This method connecting to the servers and executing commands
     * @return Map with host name as key and output of executed command as value
     */
    public static Map<String, String> start() {
        Map<String, String> serversToCheckFromFile = readServersToCheckFromFile();
        if (serversToCheckFromFile.isEmpty()) {
            LOGGER.warn("No servers to check");
            return new HashMap<>();
        }

        Set<String> serversNameToCheck = serversToCheckFromFile.keySet();

        List<ServerInfo> serversToCheck = null;/*HostOverviewService.getPhysicalServersWithCorrectPort().stream()
                .filter(server -> serversNameToCheck.contains(server.getName()))
                .toList();*/

        Map<ServerInfo, String> serversWithCommand = serversToCheck.stream()
                .collect(Collectors.toMap(
                        server -> server,
                        server -> serversToCheckFromFile.get(server.getName())
                ));

        return serversWithCommand.entrySet().stream().collect(Collectors.toMap(
                entry -> entry.getKey().getName(),
                entry -> checkServer(entry.getKey(), entry.getValue())
        ));

    }

    /**
     * Connecting to the server by hostname form HostOverview and executing command
     * @param serverInfo - object of class @ServerInfoDTO that storing server data (hostname, port, ip)
     * @param commandToExecute - command that will be executed on server
     * @return - result(output) of executed command for serverInfo
     */
    private static String checkServer(ServerInfo serverInfo, String commandToExecute) {
        LOGGER.info("Processing server -> `{}` with command -> `{}`", serverInfo.getName(), commandToExecute);
        String outputFile = null; //getSavingPath() + serverInfo.getName() + ".txt";
        String output = "";
        if (serverInfo.getConnectionType().compareToIgnoreCase("proxy") == 0) {
            LOGGER.info("Connecting to the server `{}` via proxy", serverInfo.getName());
            String proxy = getProxyIp();
            //output = RemoteCommandExecutor.execute(proxy, serverInfo.getPort(), commandToExecute);
        } else {
            LOGGER.info("Connecting to the server `{}` via IP address", serverInfo.getName());
            //output = RemoteCommandExecutor.execute(serverInfo.getIp(), serverInfo.getPort(), commandToExecute);
        }

        if (output.isEmpty()) {
            LOGGER.warn("Empty output for executing command `{}` on server `{}`", commandToExecute, serverInfo.getName());
        } else {
            writeToFile(Path.of(outputFile), output);
        }

        return output;
    }

    /**
     * Writing output of executed command in file
     * @param path file where need to be saved output
     * @param data output of executed command
     */
    private static void writeToFile(Path path, String data) {
        Path parentDir = path.getParent();

        if (!Files.exists(parentDir)) {
            try {
                LOGGER.warn("Directory `{}` does not exists! Creating directory...", parentDir.toString());
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                LOGGER.error("Unexpected error while creating directory `{}`. Error -> {}", parentDir.toString(), e.getMessage(), e);
                return;
            }
        }

        try {
            LOGGER.info("Writing data to file `{}`", path.toString());
            Files.writeString(path, data);
        } catch (IOException e) {
            LOGGER.error("Unexpected error while writing data to file `{}`. Error -> {}", path.toString(), e.getMessage(), e);
        }
    }

//    private static String getSavingPath() {
//        if (!savingPath.isEmpty()) {
//            return savingPath;
//        }
//
//        savingPath = AppConfig.get("health.report-dir");
//        if (savingPath.isEmpty()) {
//            savingPath = DEFAULT_SAVING_DIR;
//        }
//
//        return savingPath;
//    }

    private static String getProxyIp() {
        String ip = AppConfig.get("health.proxy");
        if (ip.isEmpty()) {
            LOGGER.error("No proxy IP specified! Please set up value for `health.proxy` in configuration!");
            return "";
        }
        return ip;
    }
}
