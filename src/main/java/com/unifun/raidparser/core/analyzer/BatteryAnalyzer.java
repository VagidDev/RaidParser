package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.filters.battery.*;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.parser.ReportFileParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatteryAnalyzer implements Analyzer<BatteryStatus> {
    private final ReportFileParser reportFileParser;
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
        String mainText = reportFileParser.getMainData(serverData,
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
