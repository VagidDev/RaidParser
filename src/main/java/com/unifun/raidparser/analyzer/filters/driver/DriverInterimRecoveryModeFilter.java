package com.unifun.raidparser.analyzer.filters.driver;

import com.unifun.raidparser.analyzer.filters.AbstractFilter;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;

public class DriverInterimRecoveryModeFilter extends AbstractFilter<DriverStatus> implements DriveFilter{
    @Override
    public AnalyzeResponse<DriverStatus> filter(String text) {
        if (text.contains("interim recovery mode")) {
            AnalyzeResponse<DriverStatus> response = new AnalyzeResponse<>();
            response.setStatus(DriverStatus.INTERIM_RECOVERY_MODE);
            response.setErrorText(buildErrorText(text, "interim recovery mode", "failed"));
            return response;
        }

        return new AnalyzeResponse<>(DriverStatus.OK, "");
    }
}
