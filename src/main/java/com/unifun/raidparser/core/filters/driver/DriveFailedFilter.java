package com.unifun.raidparser.core.filters.driver;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class DriveFailedFilter extends AbstractFilter<DriverStatus> implements DriveFilter {
    @Override
    public AnalyzeResponse<DriverStatus> filter(String text) {
        if (text.contains("failed")) {
//            AnalyzeResponse response = new AnalyzeResponse();
//            response.setStatus("OK(Failed)");
//            response.setErrorText(buildErrorText(text, "logicaldrive", "failed"));
            AnalyzeResponse<DriverStatus> response = new AnalyzeResponse<>();
            response.setStatus(DriverStatus.FAILED);
            response.setErrorText(buildErrorText(text, "logicaldrive", "failed"));
            return response;
        }
        return new AnalyzeResponse<>(DriverStatus.OK, "");
    }
}
