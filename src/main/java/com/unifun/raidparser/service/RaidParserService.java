package com.unifun.raidparser.service;

import com.unifun.raidparser.parser.RaidParser;
import com.unifun.raidparser.analyzer.BatteryAnalyzer;
import com.unifun.raidparser.analyzer.DriveAnalyzer;
import com.unifun.raidparser.analyzer.DriveManualAnalyzer;
import com.unifun.raidparser.analyzer.PowerSupplyAnalyzer;
import com.unifun.raidparser.analyzer.filters.battery.BatteryStatus;
import com.unifun.raidparser.analyzer.filters.driver.DriverStatus;
import com.unifun.raidparser.analyzer.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;
import com.unifun.raidparser.handlers.FileDataHandler;
import com.unifun.raidparser.handlers.ServerDataHandler;
import com.unifun.raidparser.core.DataSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class RaidParserService {
    private static final Logger LOGGER = LogManager.getLogger(RaidParserService.class);
    private final ServerDataHandler serverDataHandler = new ServerDataHandler();

    private final DriveAnalyzer driveAnalyzer = new DriveAnalyzer();
    private final DriveManualAnalyzer driveManualAnalyzer = new DriveManualAnalyzer();
    private final PowerSupplyAnalyzer powerSupplyAnalyzer = new PowerSupplyAnalyzer();
    private final BatteryAnalyzer batteryAnalyzer = new BatteryAnalyzer();

    private final RaidParser<DriverStatus> driverStatusRaidParser = new RaidParser<>();
    private final RaidParser<PowerSupplyStatus> powerSupplyStatusRaidParser = new RaidParser<>();
    private final RaidParser<BatteryStatus> batteryStatusRaidParser = new RaidParser<>();

    private final DataSorter<DriverStatus> driverStatusDataSorter = new DataSorter<>();
    private final DataSorter<PowerSupplyStatus> powerSupplyStatusDataSorter = new DataSorter<>();
    private final DataSorter<BatteryStatus> batteryStatusDataSorter = new DataSorter<>();

    public ServerDataHandler getServersDataHandler() {
        return serverDataHandler;
    }

    public int getAllRaidStatusAndWriteToFiles(String reportPath, String driveOutputPath, String batteryOutputPath, String psuOutputPath) {
        if (reportPath == null || reportPath.isBlank()) {
            LOGGER.info("file for parsing is empty, file path: {}", reportPath);
            return 0;
        }

        List<Map.Entry<String, AnalyzeResponse<DriverStatus>>> sortedDriveStatuses = getSortedDrivesStatusWithManualServers(reportPath);
        List<Map.Entry<String, AnalyzeResponse<PowerSupplyStatus>>> sortedPowerSupplyStatuses = getSortedPowerSuppliesStatus(reportPath);
        List<Map.Entry<String, AnalyzeResponse<BatteryStatus>>> sortedBatteryStatuses = getSortedBatteriesStatus(reportPath);

        FileDataHandler.writeData(driveOutputPath, sortedDriveStatuses);
        FileDataHandler.writeData(psuOutputPath, sortedPowerSupplyStatuses);
        FileDataHandler.writeData(batteryOutputPath, sortedBatteryStatuses);

        LOGGER.info("Parsing complete, count of parsed servers for: drive status -> {}, power supply status -> {}, battery status -> {}",
                sortedDriveStatuses.size(), sortedPowerSupplyStatuses.size(), sortedBatteryStatuses.size());
        // TODO: create normal return status
        return 999;
    }

    @Deprecated
    public List<Map.Entry<String, AnalyzeResponse<DriverStatus>>> getSortedDrivesStatus(String reportPath) {
        Map<String, String> serversData = serverDataHandler.getServerData(reportPath);
        Map<String, AnalyzeResponse<DriverStatus>> driveServerStatuses = driverStatusRaidParser.getParsedData(serversData, driveAnalyzer);
        return driverStatusDataSorter.sortByStatus(driveServerStatuses);
    }

    public List<Map.Entry<String, AnalyzeResponse<DriverStatus>>> getSortedDrivesStatusWithManualServers(String reportPath) {
        Map<String, String> serversData = serverDataHandler.getServerData(reportPath);
//        Map<String, String> manualServersData = ServerHealthCheckService.start();
        Map<String, AnalyzeResponse<DriverStatus>> driveServerStatuses = driverStatusRaidParser.getParsedData(serversData, driveAnalyzer);
//        Map<String, AnalyzeResponse<DriverStatus>> manualDriveServerStatuses = driverStatusRaidParser.getParsedData(manualServersData, driveManualAnalyzer);
        //Merging of statuses from manual Analyzer and default Analyzer
//        manualDriveServerStatuses.forEach((server, status) -> {
//            driveServerStatuses.merge(server, status, (currentStatus, newStatus) -> {
//                if (newStatus.getStatus() == DriverStatus.INTERIM_RECOVERY_MODE) {
//                    return newStatus;
//                } else if (newStatus.getStatus() == DriverStatus.EMPTY || newStatus.getStatus() == DriverStatus.UNKNOW) {
//                    return currentStatus;
//                } else {
//                    return newStatus;
//                }
//            });
//        });

        return driverStatusDataSorter.sortByStatus(driveServerStatuses);
    }

    public List<Map.Entry<String, AnalyzeResponse<PowerSupplyStatus>>> getSortedPowerSuppliesStatus(String reportPath) {
        Map<String, String> serversData = serverDataHandler.getServerData(reportPath);
        Map<String, AnalyzeResponse<PowerSupplyStatus>> powerSupplyServerStatuses = powerSupplyStatusRaidParser.getParsedData(serversData, powerSupplyAnalyzer);
        return powerSupplyStatusDataSorter.sortByStatus(powerSupplyServerStatuses);
    }

    public List<Map.Entry<String, AnalyzeResponse<BatteryStatus>>> getSortedBatteriesStatus(String reportPath) {
        Map<String, String> serversData = serverDataHandler.getServerData(reportPath);
        Map<String, AnalyzeResponse<BatteryStatus>> batteryServerStatuses = batteryStatusRaidParser.getParsedData(serversData, batteryAnalyzer);
        return batteryStatusDataSorter.sortByStatus(batteryServerStatuses);
    }
}
