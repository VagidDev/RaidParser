package com.unifun.raidparser.core.filters.driver;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;

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
