package com.unifun.raidparser.analyzer.filters.battery;

import com.unifun.raidparser.analyzer.filters.AbstractFilter;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;

public class BatteryFailedFilter extends AbstractFilter<BatteryStatus> implements BatteryFilter {
    @Override
    public AnalyzeResponse<BatteryStatus> filter(String text) {
        if (text.contains("failed (replace batteries)"))
            return new AnalyzeResponse<>(BatteryStatus.FAILED,
                    buildErrorText(text, "failed (replace batteries)"));
        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }
}
