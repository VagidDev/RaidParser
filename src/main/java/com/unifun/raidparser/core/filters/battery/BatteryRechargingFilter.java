package com.unifun.raidparser.core.filters.battery;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class BatteryRechargingFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("recharging")) {
            return new AnalyzeResponse<>(BatteryStatus.RECHARGING, buildErrorText(text, "recharging"));
        }
        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }
}
