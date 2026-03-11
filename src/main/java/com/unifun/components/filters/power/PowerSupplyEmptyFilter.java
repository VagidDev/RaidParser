package com.unifun.components.filters.power;

import com.unifun.components.filters.EmptyFilter;
import com.unifun.components.response.AnalyzeResponse;

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
