package com.unifun.raidparser.core.filters.battery.hpe;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.battery.BatteryFilter;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.util.TextSearcher;

public class BatteryNotPresentFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public boolean filter(String text) {
        return (TextSearcher.containsAll(text, "count: 0")
                || (TextSearcher.containsAll(text, "no-battery write cache: enabled")
                && !TextSearcher.containsAll(text, "battery/capacitor count"))
        );
    }

    @Override
    public AnalyzeResponse<BatteryStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                BatteryStatus.NO_BATTERY,
                buildErrorText(text, "count: 0", "no-battery write cache: enabled")
        );
    }
}
