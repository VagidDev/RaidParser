package com.unifun.raidparser.core.filters.battery;

import com.unifun.raidparser.core.filters.EmptyFilter;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.core.vendors.Ssacli;
import org.springframework.stereotype.Component;

@Component
@Ssacli
public class BatteryEmptyFilter extends EmptyFilter<BatteryStatus> {
    @Override
    public AnalyzeResponse<BatteryStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(BatteryStatus.EMPTY, "Empty configuration! Please check configuration manually");
    }
}
