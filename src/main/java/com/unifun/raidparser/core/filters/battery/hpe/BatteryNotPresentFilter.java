package com.unifun.raidparser.core.filters.battery.hpe;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.battery.BatteryFilter;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class BatteryNotPresentFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public boolean filter(String text) {
        return (text.contains("count: 0")
                || (text.contains("no-battery write cache: enabled")
                && !text.contains("battery/capacitor count"))
        );
    }

    @Override
    public AnalyzeResponse<BatteryStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(BatteryStatus.NO_BATTERY,
                buildErrorText(text, "count: 0", "no-battery write cache: enabled")
        );
    }
}
