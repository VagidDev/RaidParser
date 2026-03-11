package com.unifun.components.filters.battery;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class BatteryNotPresentFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("count: 0")) {
            return new AnalyzeResponse<>(BatteryStatus.NO_BATTERY,
                    buildErrorText(text, "count: 0"));
        }

        if (text.contains("no-battery write cache: enabled") && !text.contains("battery/capacitor count")) {
            return new AnalyzeResponse<>(BatteryStatus.NO_BATTERY,
                    buildErrorText(text, "no-battery write cache: enabled"));
        }

        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }
}
