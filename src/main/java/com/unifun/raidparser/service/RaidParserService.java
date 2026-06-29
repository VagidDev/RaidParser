package com.unifun.raidparser.service;

import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.parser.RaidStatusParser;
import com.unifun.raidparser.core.analyzer.BatteryAnalyzer;
import com.unifun.raidparser.core.analyzer.DriveAnalyzer;
import com.unifun.raidparser.core.analyzer.DriveManualAnalyzer;
import com.unifun.raidparser.core.analyzer.PowerSupplyAnalyzer;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.filters.driver.DriverStatus;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
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

    private final ServerDataHandler serverDataHandler;

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

    private final ServerHealthCheckService serverHealthCheckService;

    public List<ServerStatus<DriverStatus>> getSortedDrivesStatus(Path reportFilePath) {
        List<ServerData> serversData = serverDataHandler.getServerData(reportFilePath);
        List<ServerStatus<DriverStatus>> driveServerStatuses = driverStatusRaidParser.getParsedData(serversData, driveAnalyzer);
        return driverStatusDataSorter.sortByStatus(driveServerStatuses);
    }

    public List<ServerStatus<PowerSupplyStatus>> getSortedPowerSuppliesStatus(Path reportFilePath) {
        List<ServerData> serversData = serverDataHandler.getServerData(reportFilePath);
        List<ServerStatus<PowerSupplyStatus>> powerSupplyServerStatuses = powerSupplyStatusRaidParser.getParsedData(serversData, powerSupplyAnalyzer);
        return powerSupplyStatusDataSorter.sortByStatus(powerSupplyServerStatuses);
    }

    public List<ServerStatus<BatteryStatus>> getSortedBatteriesStatus(Path reportFilePath) {
        List<ServerData> serversData = serverDataHandler.getServerData(reportFilePath);
        List<ServerStatus<BatteryStatus>> batteryServerStatuses = batteryStatusRaidParser.getParsedData(serversData, batteryAnalyzer);
        return batteryStatusDataSorter.sortByStatus(batteryServerStatuses);
    }

    public List<ServerStatus<DriverStatus>> getManualDriverStatus() {
        List<ServerData> serverHealthDataList = serverHealthCheckService.checkServers();
        List<ServerStatus<DriverStatus>> driverManualStatus = new ArrayList<>(serverHealthDataList.size());

        for (ServerData serverHealthData : serverHealthDataList) {
            driverManualStatus.add(
                    new ServerStatus<>(
                            serverHealthData.serverName(),
                            driveManualAnalyzer.analyze(serverHealthData.healthData())
                    )
            );
        }

        return driverManualStatus;
    }

    public List<ServerStatus<DriverStatus>> getSortedFullDriveStatus(Path reportFilePath) {
        List<ServerData> serversData = serverDataHandler.getServerData(reportFilePath);
        List<ServerStatus<DriverStatus>> driveServerStatuses = driverStatusRaidParser.getParsedData(serversData, driveAnalyzer);
        List<ServerStatus<DriverStatus>> manualServerStatuses = getManualDriverStatus();

        driveServerStatuses.replaceAll(serverReportStatus -> {
                    ServerStatus<DriverStatus> manualServerStatus = manualServerStatuses.stream()
                            .filter(mss -> mss.serverName().contains(serverReportStatus.serverName()))
                            .findFirst()
                            .orElse(null);
                    if (manualServerStatus == null) {
                        return serverReportStatus;
                    } else if (manualServerStatus.analyzeResponse().getStatus().getPriority() > serverReportStatus.analyzeResponse().getStatus().getPriority()) {
                        LOGGER.info(
                                "Replacing existing server status {} for server {} with manual checked. Manual checked server status is {}",
                                serverReportStatus.serverName(),
                                serverReportStatus.analyzeResponse().getStatus().getName(),
                                manualServerStatus.analyzeResponse().getStatus().getName()
                        );
                        return manualServerStatus;
                    } else {
                        return serverReportStatus;
                    }

                }
        );

        return driverStatusDataSorter.sortByStatus(driveServerStatuses);
    }
}