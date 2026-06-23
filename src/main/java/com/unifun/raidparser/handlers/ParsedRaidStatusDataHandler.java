package com.unifun.raidparser.handlers;

import com.unifun.raidparser.config.ParsedRaidStatusDataCacheConfig;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.filters.driver.DriverStatus;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.dto.ParsedDataCache;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.service.RaidParserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ParsedRaidStatusDataHandler {
    private final RaidParserService raidParserService;
    private final ParsedRaidStatusDataCacheConfig parsedRaidStatusDataCacheConfig;

    private ParsedDataCache<DriverStatus> driverStatusParsedDataCache;
    private ParsedDataCache<PowerSupplyStatus> powerSupplyStatusParsedDataCache;
    private ParsedDataCache<BatteryStatus> batteryStatusParsedDataCache;

    @PostConstruct
    private void initialize() {
        driverStatusParsedDataCache = new ParsedDataCache<>(parsedRaidStatusDataCacheConfig.getDriveStatusAgeSeconds());
        powerSupplyStatusParsedDataCache = new ParsedDataCache<>(parsedRaidStatusDataCacheConfig.getPowerSupplyStatusAgeSeconds());
        batteryStatusParsedDataCache = new ParsedDataCache<>(parsedRaidStatusDataCacheConfig.getBatteryStatusAgeSeconds());
    }

    private <T extends Status> List<ServerStatus<T>> getFromCacheOrParse(
            ParsedDataCache<T> cache,
            Path path,
            Function<Path, List<ServerStatus<T>>> parser) {
        if (cache.isDataValid(path)) {
            return cache.getServerStatusData();
        }
        List<ServerStatus<T>> data = parser.apply(path);
        cache.store(data, path);
        return data;
    }

    public List<ServerStatus<DriverStatus>> getSortedDriveStatus(Path reportFilePath) {
        return getFromCacheOrParse(driverStatusParsedDataCache, reportFilePath,
                raidParserService::getSortedDrivesStatus);
    }

    public List<ServerStatus<PowerSupplyStatus>> getSortedPowerSupplyStatus(Path reportFilePath) {
        return getFromCacheOrParse(powerSupplyStatusParsedDataCache, reportFilePath,
                raidParserService::getSortedPowerSuppliesStatus);
    }

    public List<ServerStatus<BatteryStatus>> getSortedBatteryStatus(Path reportFilePath) {
        return getFromCacheOrParse(batteryStatusParsedDataCache, reportFilePath,
                raidParserService::getSortedBatteriesStatus);
    }
}
