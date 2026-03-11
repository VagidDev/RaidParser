package com.unifun.raidparser.analyzer.filters.driver;

import com.unifun.raidparser.analyzer.filters.AbstractFilter;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;

public class DriveOkFilter extends AbstractFilter<DriverStatus> implements DriveFilter {
    @Override
    public AnalyzeResponse<DriverStatus> filter(String text) {
        if (text.contains("logicaldrive") && text.contains("physicaldrive"))
            return new AnalyzeResponse<>(DriverStatus.OK,"");
        return new AnalyzeResponse<>(DriverStatus.UNKNOW, "Cannot parse data, please check manually\n");
    }
}
