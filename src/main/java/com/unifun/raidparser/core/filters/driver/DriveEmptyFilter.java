package com.unifun.raidparser.core.filters.driver;

import com.unifun.raidparser.core.filters.EmptyFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class DriveEmptyFilter extends EmptyFilter<DriverStatus> implements DriveFilter {
    @Override
    protected AnalyzeResponse<DriverStatus> getValidResponse() {
        return new AnalyzeResponse<>(DriverStatus.OK, "");
    }

    @Override
    protected AnalyzeResponse<DriverStatus> getInvalidResponse() {
        return new AnalyzeResponse<>(DriverStatus.EMPTY, "Empty response, please check manually\n");
    }
}
