package com.unifun.components.filters.driver;

import com.unifun.components.filters.EmptyFilter;
import com.unifun.components.response.AnalyzeResponse;

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
