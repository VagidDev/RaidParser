package com.unifun.raidparser.core.analyzer.battery;

import com.unifun.raidparser.core.analyzer.AbstractAnalyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.battery.*;
import com.unifun.raidparser.core.vendors.Ssacli;
import com.unifun.raidparser.util.TextSearcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SsacliBatteryAnalyzer extends AbstractAnalyzer<BatteryStatus> {
    private final List<Filter<BatteryStatus>> batteryFilters;

    public SsacliBatteryAnalyzer(@Ssacli List<Filter<BatteryStatus>> batteryFilters) {
        this.batteryFilters = batteryFilters;
    }

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
        return TextSearcher.containsAll(text, "Smart Array", "Cache Board Present: True");
    }

    @Override
    public BatteryStatus getUnknownStatus() {
        return BatteryStatus.UNKNOWN;
    }
}
