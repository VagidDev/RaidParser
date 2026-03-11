package com.unifun.components;

import com.unifun.components.filters.battery.*;
import com.unifun.components.response.AnalyzeResponse;
import com.unifun.handlers.FileDataHandler;

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
