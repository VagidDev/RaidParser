package com.unifun.raidparser.analyzer.filters.power;

import com.unifun.raidparser.analyzer.filters.EmptyFilter;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;

public class PowerSupplyEmptyFilter extends EmptyFilter<PowerSupplyStatus> implements PowerSupplyFilter {
    @Override
    protected AnalyzeResponse<PowerSupplyStatus> getValidResponse() {
        return new AnalyzeResponse<>(PowerSupplyStatus.OK, "");
    }

    @Override
    protected AnalyzeResponse<PowerSupplyStatus> getInvalidResponse() {
        return new AnalyzeResponse<>(PowerSupplyStatus.EMPTY, "Empty response, please check manually\n");
    }
}
