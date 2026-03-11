package com.unifun.raidparser.analyzer.filters.power;

import com.unifun.raidparser.analyzer.filters.AbstractFilter;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;

public class PowerSupplyNotPresentFilter extends AbstractFilter<PowerSupplyStatus> implements PowerSupplyFilter{
    @Override
    public AnalyzeResponse<PowerSupplyStatus> filter(String text) {
        if (text.contains("power supply not present"))
            return new AnalyzeResponse<>(PowerSupplyStatus.NOT_PRESENT,
                    buildErrorText(text, "power supply not present", "Power supply #"));
        return new AnalyzeResponse<>(PowerSupplyStatus.OK, "");
    }
}
