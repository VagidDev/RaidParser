package com.unifun.components.filters.battery;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class BatteryOkFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("cache status: ok")
                && text.contains("no-battery write cache: disabled")
                && text.contains("battery/capacitor status: ok")) {
            return new AnalyzeResponse<>(BatteryStatus.OK, "");
        }

        return new AnalyzeResponse<>(BatteryStatus.UNKNOWN, "Cannot read config, please check it manually\n");
    }
}
