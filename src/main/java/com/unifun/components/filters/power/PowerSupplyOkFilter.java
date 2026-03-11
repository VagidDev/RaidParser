package com.unifun.components.filters.power;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class PowerSupplyOkFilter extends AbstractFilter<PowerSupplyStatus> implements PowerSupplyFilter {
    @Override
    public AnalyzeResponse<PowerSupplyStatus> filter(String text) {
        if ((text.contains("condition: ok") && text.contains("redundant: yes")) || text.contains("fully redundant")) {
            return new AnalyzeResponse<>(PowerSupplyStatus.OK, "");
        }
        return new AnalyzeResponse<>(PowerSupplyStatus.UNKNOWN, "Some error in configuration, please check manually\n");
    }
}
