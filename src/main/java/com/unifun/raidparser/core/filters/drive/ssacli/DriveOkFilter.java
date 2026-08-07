package com.unifun.raidparser.core.filters.drive.ssacli;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.Filter;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.core.vendors.Ssacli;
import com.unifun.raidparser.util.TextSearcher;
import org.springframework.stereotype.Component;

@Component
@Ssacli
public class DriveOkFilter extends AbstractFilter<DriverStatus> {
    @Override
    public boolean filter(String text) {
        return TextSearcher.containsAll(text, "logicaldrive", "physicaldrive");
    }

    @Override
    public AnalyzeResponse<DriverStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(DriverStatus.OK,"");
    }
}
