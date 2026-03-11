package com.unifun.raidparser.analyzer.filters.battery;

import com.unifun.raidparser.analyzer.filters.AbstractFilter;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;

public class BatteryCachePermanentlyDisabledFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("permanently disabled") && text.contains("battery/capacitor status: ok"))
            return new AnalyzeResponse<>(BatteryStatus.CACHE_DISABLED,
                    buildErrorText(text, "cache status", "battery/capacitor status"));
        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }
}
