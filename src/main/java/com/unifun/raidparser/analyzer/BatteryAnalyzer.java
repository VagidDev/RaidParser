package com.unifun.raidparser.analyzer;

import com.unifun.raidparser.analyzer.filters.battery.*;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;
import com.unifun.raidparser.handlers.FileDataHandler;

import java.util.List;

public class BatteryAnalyzer implements Analyzer<BatteryStatus> {
    private final List<BatteryFilter> batteryFilters = List.of(
            new BatteryEmptyFilter(),
            new BatteryFailedFilter(),
            new BatteryRechargingFilter(),
            new BatteryNotPresentFilter(),
            new BatteryCachePermanentlyDisabledFilter(),
            new BatteryNoBatteryWriteCacheEnableFilter(),
            new BatteryOkFilter()
    );

    public AnalyzeResponse<BatteryStatus> analyze(String serverData) {
        String mainText = FileDataHandler.getMainData(serverData,
                "=========================config===============================",
                "=========================drive================================");

        AnalyzeResponse<BatteryStatus> response = null;

        for (BatteryFilter filter : batteryFilters) {
            response = filter.filter(mainText);
            if (response.getStatus() != BatteryStatus.OK) {
                return response;
            }
        }

        return response;
    }
}
