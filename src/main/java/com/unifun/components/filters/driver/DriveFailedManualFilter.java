package com.unifun.components.filters.driver;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class DriveFailedManualFilter extends AbstractFilter<DriverStatus> implements DriveFilter {
    @Override
    public AnalyzeResponse<DriverStatus> filter(String text) {
        if (text.contains("active raid1")
                && text.contains("blocks super")
                && (text.contains("[1/2] [_U]")
                    || text.contains("[1/2] [U_]")
                    || text.contains("[2/1] [_U]")
                    || text.contains("[2/1] [U_]")
                )
        ) {
            return new AnalyzeResponse<>(DriverStatus.INTERIM_RECOVERY_MODE, buildErrorText(text, "md", "blocks super"));
        }
        return new AnalyzeResponse<>(DriverStatus.OK, "");
    }
}
