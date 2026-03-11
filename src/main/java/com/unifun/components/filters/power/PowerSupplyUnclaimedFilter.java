package com.unifun.components.filters.power;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class PowerSupplyUnclaimedFilter extends AbstractFilter<PowerSupplyStatus> implements PowerSupplyFilter {
    @Override
    public AnalyzeResponse<PowerSupplyStatus> filter(String text) {
        if (text.contains("unclaimed"))
            return new AnalyzeResponse<>(PowerSupplyStatus.UNCLAIMED,
                    buildErrorText(text, "unclaimed"));
        return new AnalyzeResponse<>(PowerSupplyStatus.OK, "");
    }
}
