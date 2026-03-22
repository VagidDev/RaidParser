package com.unifun.raidparser.service;

import com.unifun.raidparser.config.OutputStatusFileConfig;
import com.unifun.raidparser.handlers.FileDataHandler;
import com.unifun.raidparser.parser.RaidStatusParser;
import com.unifun.raidparser.core.analyzer.BatteryAnalyzer;
import com.unifun.raidparser.core.analyzer.DriveAnalyzer;
import com.unifun.raidparser.core.analyzer.DriveManualAnalyzer;
import com.unifun.raidparser.core.analyzer.PowerSupplyAnalyzer;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.filters.driver.DriverStatus;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.handlers.ServerDataHandler;
import com.unifun.raidparser.util.ServerDataSorter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RaidParserService {
    private static final Logger LOGGER = LogManager.getLogger(RaidParserService.class);
    private final OutputStatusFileConfig outputStatusFileConfig;

    private final ServerDataHandler serverDataHandler;
    private final FileDataHandler fileDataHandler;

    private final DriveAnalyzer driveAnalyzer;
    private final PowerSupplyAnalyzer powerSupplyAnalyzer;
    private final BatteryAnalyzer batteryAnalyzer;
    //TODO: need to adapt ServerHealthChecker
    private final DriveManualAnalyzer driveManualAnalyzer;

    private final RaidStatusParser<DriverStatus> driverStatusRaidParser;
    private final RaidStatusParser<PowerSupplyStatus> powerSupplyStatusRaidParser;
    private final RaidStatusParser<BatteryStatus> batteryStatusRaidParser;

    private final ServerDataSorter<DriverStatus> driverStatusDataSorter;
    private final ServerDataSorter<PowerSupplyStatus> powerSupplyStatusDataSorter;
    private final ServerDataSorter<BatteryStatus> batteryStatusDataSorter;

    public List<Map.Entry<String, AnalyzeResponse<DriverStatus>>> getSortedDrivesStatus(Path reportFilePath) {
        Map<String, String> serversData = serverDataHandler.getServerData(reportFilePath);
        Map<String, AnalyzeResponse<DriverStatus>> driveServerStatuses = driverStatusRaidParser.getParsedData(serversData, driveAnalyzer);
        return driverStatusDataSorter.sortByStatus(driveServerStatuses);
    }

    public List<Map.Entry<String, AnalyzeResponse<PowerSupplyStatus>>> getSortedPowerSuppliesStatus(Path reportFilePath) {
        Map<String, String> serversData = serverDataHandler.getServerData(reportFilePath);
        Map<String, AnalyzeResponse<PowerSupplyStatus>> powerSupplyServerStatuses = powerSupplyStatusRaidParser.getParsedData(serversData, powerSupplyAnalyzer);
        return powerSupplyStatusDataSorter.sortByStatus(powerSupplyServerStatuses);
    }

    public List<Map.Entry<String, AnalyzeResponse<BatteryStatus>>> getSortedBatteriesStatus(Path reportFilePath) {
        Map<String, String> serversData = serverDataHandler.getServerData(reportFilePath);
        Map<String, AnalyzeResponse<BatteryStatus>> batteryServerStatuses = batteryStatusRaidParser.getParsedData(serversData, batteryAnalyzer);
        return batteryStatusDataSorter.sortByStatus(batteryServerStatuses);
    }

    public int writeSortedDriveStatusToFile(Path reportFilePath, Path driveStatusFilePath) {
        List<Map.Entry<String, AnalyzeResponse<DriverStatus>>> sortedDriveStatus = getSortedDrivesStatus(reportFilePath);
        fileDataHandler.writeData(driveStatusFilePath, sortedDriveStatus);
        return sortedDriveStatus.size();
    }

    public int writeSortedPowerSupplyUnitStatusToFile(Path reportFilePath, Path psuStatusFilePath) {
        List<Map.Entry<String, AnalyzeResponse<PowerSupplyStatus>>> sortedPowerSuppliesStatus = getSortedPowerSuppliesStatus(reportFilePath);
        fileDataHandler.writeData(psuStatusFilePath, sortedPowerSuppliesStatus);
        return sortedPowerSuppliesStatus.size();
    }

    public int writeSortedBatteryStatusToFile(Path reportFilePath, Path baterryStatusFilePath) {
        List<Map.Entry<String, AnalyzeResponse<BatteryStatus>>> sortedBatteriesStatus = getSortedBatteriesStatus(reportFilePath);
        fileDataHandler.writeData(baterryStatusFilePath, sortedBatteriesStatus);
        return sortedBatteriesStatus.size();
    }

    public int writeSortedDriveStatusToFile(Path reportFilePath) {
        Path driveStatusFilePath = Path.of(outputStatusFileConfig.getDriveStatus());
        return writeSortedDriveStatusToFile(reportFilePath, driveStatusFilePath);
    }

    public int writeSortedPowerSupplyUnitStatusToFile(Path reportFilePath) {
        Path psuStatusFilePath = Path.of(outputStatusFileConfig.getPsuStatus());
        return writeSortedPowerSupplyUnitStatusToFile(reportFilePath, psuStatusFilePath);
    }

    public int writeSortedBatteryStatusToFile(Path reportFilePath) {
        Path baterryStatusFilePath = Path.of(outputStatusFileConfig.getBatteryStatus());
        return writeSortedBatteryStatusToFile(reportFilePath, baterryStatusFilePath);
    }

}
//    public List<Map.Entry<String, AnalyzeResponse<DriverStatus>>> getSortedDrivesStatusWithManualServers(String reportPath) {
//        Map<String, String> serversData = serverDataHandler.getServerData(reportPath);
//        Map<String, AnalyzeResponse<DriverStatus>> driveServerStatuses = driverStatusRaidParser.getParsedData(serversData, driveAnalyzer);
//        return driverStatusDataSorter.sortByStatus(driveServerStatuses);
//    }

//        Map<String, String> manualServersData = ServerHealthCheckService.start();
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