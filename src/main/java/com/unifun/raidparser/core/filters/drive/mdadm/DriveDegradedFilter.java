package com.unifun.raidparser.core.filters.drive.mdadm;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.drive.DriveFilter;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;

public class DriveDegradedFilter extends AbstractFilter<DriverStatus> implements DriveFilter {

    @Override
    public boolean filter(String text) {
        return (text.contains("active raid1")
                && text.contains("blocks super")
                && (text.contains("[1/2] [_U]")
                || text.contains("[1/2] [U_]")
                || text.contains("[2/1] [_U]")
                || text.contains("[2/1] [U_]")
        )
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
