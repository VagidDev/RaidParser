package com.unifun.raidparser.core.filters.drive.mdadm;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.drive.DriveFilter;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.util.TextSearcher;

public class DriveDegradedFilter extends AbstractFilter<DriverStatus> implements DriveFilter {

    @Override
    public boolean filter(String text) {
        return (TextSearcher.containsAll(text, "active raid1", "blocks super")
                && TextSearcher.containsAny(text, "[1/2] [_U]", "[1/2] [U_]","[2/1] [_U]","[2/1] [U_]")
        );
    }

    @Override
    public AnalyzeResponse<DriverStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                DriverStatus.INTERIM_RECOVERY_MODE,
                buildErrorText(text, "md", "blocks super")
        );
    }
}
