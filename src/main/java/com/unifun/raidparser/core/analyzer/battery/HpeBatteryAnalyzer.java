package com.unifun.raidparser.core.analyzer.battery;

import com.unifun.raidparser.core.analyzer.AbstractAnalyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.battery.*;
import com.unifun.raidparser.core.filters.battery.hpe.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HpeBatteryAnalyzer extends AbstractAnalyzer<BatteryStatus> {
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
    public HealthType getSupportedType() {
        return HealthType.BATTERY_HEALTH;
    }

    @Override
    protected List<Filter<BatteryStatus>> getFilters() {
        return batteryFilters;
    }

    @Override
    public boolean isSupportedRawData(String text) {
        return text.contains("Smart Array") && text.contains("Cache Board Present: True");
    }

    @Override
    protected BatteryStatus getUnknownStatus() {
        return BatteryStatus.UNKNOWN;
    }
}
