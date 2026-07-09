package com.unifun.raidparser.core.filters.battery.hpe;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.battery.BatteryFilter;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.util.TextSearcher;

public class BatteryNoBatteryWriteCacheEnableFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public boolean filter(String text) {
        return TextSearcher.containsAll(
                text,
                "no-battery write cache: enabled", "battery/capacitor status: ok");
    }

    @Override
    public AnalyzeResponse<BatteryStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                BatteryStatus.NOT_SAFE,
                buildErrorText(text, "no-battery write cache: enabled", "battery/capacitor status: ok")
        );
    }
}
