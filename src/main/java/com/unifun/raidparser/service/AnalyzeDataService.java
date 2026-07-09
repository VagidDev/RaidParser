package com.unifun.raidparser.service;

import com.unifun.raidparser.builder.ServerStatusBuilder;
import com.unifun.raidparser.core.analyzer.Analyzer;
import com.unifun.raidparser.core.analyzer.battery.HpeBatteryAnalyzer;
import com.unifun.raidparser.core.analyzer.drive.HpeDriveAnalyzer;
import com.unifun.raidparser.core.analyzer.drive.MdadmDriveAnalyzer;
import com.unifun.raidparser.core.analyzer.psu.IpmitoolPowerSupplyAnalyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Component
public class AnalyzeDataService {
    private static final Logger LOGGER = LogManager.getLogger(AnalyzeDataService.class);
    private static final List<Analyzer<DriverStatus>> DRIVE_ANALYZERS = List.of(
            new HpeDriveAnalyzer(),
            new MdadmDriveAnalyzer()
    );
    private static final List<Analyzer<PowerSupplyStatus>> PSU_ANALYZERS = List.of(
            new IpmitoolPowerSupplyAnalyzer()
    );
    private static final List<Analyzer<BatteryStatus>> BATTERY_ANALYZERS = List.of(
            new HpeBatteryAnalyzer()
    );

    //private final ServerStatusDataHandler

    public ServerStatus analyze(ServerData serverData) {
        if (serverData == null) {
            //log
            return null;
        }

        if (CollectionUtils.isEmpty(serverData.rawDataByComponent())) {
            //log
            return null;
        }

        ServerStatusBuilder serverStatusBuilder = new ServerStatusBuilder().serverName(serverData.serverName());

        for (Map.Entry<HealthType, String> entry : serverData.rawDataByComponent().entrySet()) {
            AnalyzeResponse<? extends Status> response = analyzeServerData(entry.getKey(), entry.getValue());
            serverStatusBuilder.addHealthStatus(entry.getKey(), response);
        }

        return serverStatusBuilder.build();
    }

    private AnalyzeResponse<? extends Status> analyzeServerData(HealthType healthType, String rawData){
        return switch (healthType) {
            case DRIVE_HEALTH -> analyzeDriveData(rawData);
            case PSU_HEALTH -> analyzePowerSupplyData(rawData);
            case BATTERY_HEALTH -> analyzeBatteryData(rawData);
        };
    }

    private AnalyzeResponse<DriverStatus> analyzeDriveData(String rawData) {
        for (Analyzer<DriverStatus> analyzer : DRIVE_ANALYZERS) {
            if (analyzer.isSupportedRawData(rawData)) {
                return analyzer.analyze(rawData);
            }
        }
        LOGGER.warn("Cannot analyze drive status with data {}", rawData);
        return new AnalyzeResponse<>(DriverStatus.UNSUPPORTED_TYPE, "Unsupported drive data");
    }

    private AnalyzeResponse<PowerSupplyStatus> analyzePowerSupplyData(String rawData) {
        for (Analyzer<PowerSupplyStatus> analyzer : PSU_ANALYZERS) {
            if (analyzer.isSupportedRawData(rawData)) {
                return analyzer.analyze(rawData);
            }
        }
        LOGGER.warn("Cannot analyze power supply status with data {}", rawData);
        return new AnalyzeResponse<>(PowerSupplyStatus.UNSUPPORTED_TYPE, "Unsupported power supply data");
    }

    private AnalyzeResponse<BatteryStatus> analyzeBatteryData(String rawData) {
        for (Analyzer<BatteryStatus> analyzer : BATTERY_ANALYZERS) {
            if (analyzer.isSupportedRawData(rawData)) {
                return analyzer.analyze(rawData);
            }
        }
        LOGGER.warn("Cannot analyze battery status with data {}", rawData);
        return new AnalyzeResponse<>(BatteryStatus.UNSUPPORTED_TYPE, "Unsupported power supply data");
    }

}
