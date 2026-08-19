package com.unifun.raidparser.core.filters.drive.ssacli;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.core.vendors.Ssacli;
import com.unifun.raidparser.util.TextSearcher;
import org.springframework.stereotype.Component;

@Component
@Ssacli
public class DriveFailedFilter extends AbstractFilter<DriverStatus> {
    @Override
    public boolean filter(String text) {
        return TextSearcher.containsAll(text, "failed");
    }

    @Override
    public AnalyzeResponse<DriverStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                DriverStatus.FAILED,
                buildErrorText(text, "logicaldrive", "failed")
        );
    }
}
