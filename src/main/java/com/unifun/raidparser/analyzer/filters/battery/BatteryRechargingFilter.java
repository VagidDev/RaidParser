package com.unifun.raidparser.analyzer.filters.battery;

import com.unifun.raidparser.analyzer.filters.AbstractFilter;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;

public class BatteryRechargingFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("recharging")) {
            return new AnalyzeResponse<>(BatteryStatus.RECHARGING, buildErrorText(text, "recharging"));
        }
        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }
}
