package com.unifun.components.filters.battery;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class BatteryFailedFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("failed (replace batteries)"))
            return new AnalyzeResponse<>(BatteryStatus.FAILED,
                    buildErrorText(text, "failed (replace batteries)"));
        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }
}
