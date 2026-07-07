package com.unifun.raidparser.core.filters.drive;

import com.unifun.raidparser.core.filters.EmptyFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class DriveEmptyFilter extends EmptyFilter<DriverStatus> implements DriveFilter {
    @Override
    public AnalyzeResponse<DriverStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(DriverStatus.EMPTY, "Empty response, please check manually");
    }
}
