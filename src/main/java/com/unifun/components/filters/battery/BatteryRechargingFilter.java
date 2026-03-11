package com.unifun.components.filters.battery;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class BatteryRechargingFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("recharging")) {
            return new AnalyzeResponse<>(BatteryStatus.RECHARGING, buildErrorText(text, "recharging"));
        }
        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }
}
