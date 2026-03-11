package com.unifun.components.filters.driver;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

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
