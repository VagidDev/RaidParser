package com.unifun.raidparser.core.analyzer;

import com.unifun.raidparser.core.component.ComponentType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.battery.*;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.parser.ReportFileParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatteryAnalyzer extends AbstractAnalyzer<BatteryStatus> {
    private final List<Filter<BatteryStatus>> batteryFilters = List.of(
            new BatteryEmptyFilter(),
            new BatteryFailedFilter(),
            new BatteryRechargingFilter(),
            new BatteryNotPresentFilter(),
            new BatteryCachePermanentlyDisabledFilter(),
            new BatteryNoBatteryWriteCacheEnableFilter(),
            new BatteryOkFilter()
    );

    @Override
    public ComponentType getSupportedType() {
        return ComponentType.BATTERY_HEALTH;
    }


    @Override
    protected List<Filter<BatteryStatus>> getFilters() {
        return batteryFilters;
    }

    @Override
    protected BatteryStatus getSuccessfulStatus() {
        return BatteryStatus.OK;
    }
}
