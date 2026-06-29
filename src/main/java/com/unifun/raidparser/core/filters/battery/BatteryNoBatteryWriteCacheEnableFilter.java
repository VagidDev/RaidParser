package com.unifun.raidparser.core.filters.battery;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class BatteryNoBatteryWriteCacheEnableFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("no-battery write cache: enabled") && text.contains("battery/capacitor status: ok"))
            return new AnalyzeResponse<>(BatteryStatus.NOT_SAFE,
                    buildErrorText(text, "no-battery write cache: enabled", "battery/capacitor status: ok"));

        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }
}
