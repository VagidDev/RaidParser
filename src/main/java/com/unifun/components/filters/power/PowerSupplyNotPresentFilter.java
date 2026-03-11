package com.unifun.components.filters.power;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class PowerSupplyNotPresentFilter extends AbstractFilter<PowerSupplyStatus> implements PowerSupplyFilter{
    @Override
    public AnalyzeResponse<PowerSupplyStatus> filter(String text) {
        if (text.contains("power supply not present"))
            return new AnalyzeResponse<>(PowerSupplyStatus.NOT_PRESENT,
                    buildErrorText(text, "power supply not present", "Power supply #"));
        return new AnalyzeResponse<>(PowerSupplyStatus.OK, "");
    }
}
