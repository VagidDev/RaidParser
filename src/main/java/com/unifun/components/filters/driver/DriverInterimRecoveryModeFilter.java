package com.unifun.components.filters.driver;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

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
