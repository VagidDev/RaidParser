package com.unifun.raidparser.core.filters.battery;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class BatteryFailedFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("failed (replace batteries)"))
            return new AnalyzeResponse<>(BatteryStatus.FAILED,
                    buildErrorText(text, "failed (replace batteries)"));
        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }
}
