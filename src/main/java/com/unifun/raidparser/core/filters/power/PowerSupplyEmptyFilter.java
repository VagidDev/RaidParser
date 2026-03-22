package com.unifun.raidparser.core.filters.power;

import com.unifun.raidparser.core.filters.EmptyFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

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
