package com.unifun.raidparser.core.filters.battery.hpe;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.battery.BatteryFilter;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.util.TextSearcher;

public class BatteryCachePermanentlyDisabledFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public boolean filter(String text) {
        return TextSearcher.containsAll(text,"permanently disabled","battery/capacitor status: ok");
    }

    @Override
    public AnalyzeResponse<BatteryStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                BatteryStatus.CACHE_DISABLED,
                buildErrorText(text, "cache status", "battery/capacitor status")
        );
    }
}
