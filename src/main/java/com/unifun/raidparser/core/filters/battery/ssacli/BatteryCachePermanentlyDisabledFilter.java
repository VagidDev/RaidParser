package com.unifun.raidparser.core.filters.battery.ssacli;

import com.unifun.raidparser.core.filters.AbstractFilter;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.core.vendors.Ssacli;
import com.unifun.raidparser.util.TextSearcher;
import org.springframework.stereotype.Component;

@Component
@Ssacli
public class BatteryCachePermanentlyDisabledFilter extends AbstractFilter<BatteryStatus> {
    @Override
    public boolean filter(String text) {
        return TextSearcher.containsAll(text,"permanently disabled","battery/capacitor status: ok");
    }

    @Override
    public AnalyzeResponse<BatteryStatus> getFilterResponse(String text) {
        return new AnalyzeResponse<>(
                BatteryStatus.CACHE_DISABLED,
                buildErrorText(text, "cache status", "battery/capacitor status")
        );
    }
}
