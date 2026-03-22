package com.unifun.raidparser.core.filters.power;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class PowerSupplyUnclaimedFilter extends AbstractFilter<PowerSupplyStatus> implements PowerSupplyFilter {
    @Override
    public AnalyzeResponse<PowerSupplyStatus> filter(String text) {
        if (text.contains("unclaimed"))
            return new AnalyzeResponse<>(PowerSupplyStatus.UNCLAIMED,
                    buildErrorText(text, "unclaimed"));
        return new AnalyzeResponse<>(PowerSupplyStatus.OK, "");
    }
}
