package com.unifun.raidparser.core.filters.battery.hpe;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.battery.BatteryFilter;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class BatteryNoBatteryWriteCacheEnableFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public boolean filter(String text) {
        return (text.contains("no-battery write cache: enabled")
                && text.contains("battery/capacitor status: ok")
        );
    }

    @Override
    public AnalyzeResponse<BatteryStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                BatteryStatus.NOT_SAFE,
                buildErrorText(text, "no-battery write cache: enabled", "battery/capacitor status: ok")
        );
    }
}
