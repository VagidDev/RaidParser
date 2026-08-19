package com.unifun.raidparser.core.filters.drive;

import com.unifun.raidparser.core.filters.EmptyFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.core.vendors.Mdadm;
import com.unifun.raidparser.core.vendors.Ssacli;
import org.springframework.stereotype.Component;

@Component
@Ssacli
@Mdadm
public class DriveEmptyFilter extends EmptyFilter<DriverStatus> {
    @Override
    public AnalyzeResponse<DriverStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(DriverStatus.EMPTY, "Empty response, please check manually");
    }
}
