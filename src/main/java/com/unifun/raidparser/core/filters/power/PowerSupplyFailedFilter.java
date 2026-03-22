package com.unifun.raidparser.core.filters.power;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class PowerSupplyFailedFilter extends AbstractFilter<PowerSupplyStatus> implements PowerSupplyFilter {
    @Override
    public AnalyzeResponse<PowerSupplyStatus> filter(String text) {
        if (text.contains("condition: failed") || text.contains("failure detected"))
            return new AnalyzeResponse<>(PowerSupplyStatus.FAILED,
                    buildErrorText(text, "condition: failed", "failure detected", "power supply #"));
        return new AnalyzeResponse<>(PowerSupplyStatus.OK, "");
    }
}
