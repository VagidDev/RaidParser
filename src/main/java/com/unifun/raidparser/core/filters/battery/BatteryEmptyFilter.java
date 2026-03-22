package com.unifun.raidparser.core.filters.battery;

import com.unifun.raidparser.core.filters.EmptyFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class BatteryEmptyFilter extends EmptyFilter<BatteryStatus> implements BatteryFilter {
    @Override
    protected AnalyzeResponse<BatteryStatus> getValidResponse() {
        return new AnalyzeResponse<>(BatteryStatus.OK, "");
    }

    @Override
    protected AnalyzeResponse<BatteryStatus> getInvalidResponse() {
        return new AnalyzeResponse<>(BatteryStatus.EMPTY, "Empty configuration! Please check configuration manually\n");
    }
}
