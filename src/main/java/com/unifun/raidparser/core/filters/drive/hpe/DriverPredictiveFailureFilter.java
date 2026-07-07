package com.unifun.raidparser.core.filters.drive.hpe;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.drive.DriveFilter;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class DriverPredictiveFailureFilter extends AbstractFilter<DriverStatus> implements DriveFilter {
    @Override
    public boolean filter(String text) {
        return text.contains("predictive failure");
    }

    @Override
    public AnalyzeResponse<DriverStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                DriverStatus.PREDICTIVE_FAILURE,
                buildErrorText(text, "logicaldrive", "predictive failure")
        );
    }
}
