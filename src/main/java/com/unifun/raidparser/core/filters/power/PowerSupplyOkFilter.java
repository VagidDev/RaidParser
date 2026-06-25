package com.unifun.raidparser.core.filters.power;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class PowerSupplyOkFilter extends AbstractFilter<PowerSupplyStatus> implements PowerSupplyFilter {
    @Override
    public AnalyzeResponse<PowerSupplyStatus> filter(String text) {
        if ((text.contains("condition: ok") && text.contains("redundant: yes")) || text.contains("fully redundant")) {
            return new AnalyzeResponse<>(PowerSupplyStatus.OK, "");
        }
        return new AnalyzeResponse<>(PowerSupplyStatus.UNKNOWN, "Some error in configuration, please check manually");
    }
}
