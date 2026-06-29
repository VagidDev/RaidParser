package com.unifun.raidparser.core.filters.driver;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class DriverPredictiveFailureFilter extends AbstractFilter<DriverStatus> implements DriveFilter {
    @Override
    public AnalyzeResponse<DriverStatus> filter(String text) {
        if (text.contains("predictive failure")) {
            AnalyzeResponse<DriverStatus> response = new AnalyzeResponse<>();
            response.setStatus(DriverStatus.PREDICTIVE_FAILURE);
            response.setErrorText(buildErrorText(text, "logicaldrive", "predictive failure"));
            return response;
        }
        return new AnalyzeResponse<>(DriverStatus.OK, "");
    }
}
