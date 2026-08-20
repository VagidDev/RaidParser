package com.unifun.raidparser.core.filters.drive.mdadm;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.core.vendors.Mdadm;
import org.springframework.stereotype.Component;

@Component
@Mdadm
public class DriveOkFilter extends AbstractFilter<DriverStatus> {
    @Override
    public boolean filter(String text) {
        return MdstatStatusLine.hasAnyArray(text) && MdstatStatusLine.problemLines(text).isEmpty();
    }

    @Override
    public AnalyzeResponse<DriverStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                DriverStatus.OK,
                ""
        );
    }
}
