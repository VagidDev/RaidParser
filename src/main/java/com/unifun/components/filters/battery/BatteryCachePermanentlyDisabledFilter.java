package com.unifun.components.filters.battery;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class BatteryCachePermanentlyDisabledFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("permanently disabled") && text.contains("battery/capacitor status: ok"))
            return new AnalyzeResponse<>(BatteryStatus.CACHE_DISABLED,
                    buildErrorText(text, "cache status", "battery/capacitor status"));
        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }
}
