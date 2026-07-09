package com.unifun.raidparser.core.filters.drive.hpe;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.drive.DriveFilter;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.util.TextSearcher;

public class DriverInterimRecoveryModeFilter extends AbstractFilter<DriverStatus> implements DriveFilter {
    @Override
    public boolean filter(String text) {
        return TextSearcher.containsAll(text, "interim recovery mode");
    }

    @Override
    public AnalyzeResponse<DriverStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                DriverStatus.INTERIM_RECOVERY_MODE,
                buildErrorText(text, "interim recovery mode", "failed")
        );
    }
}
