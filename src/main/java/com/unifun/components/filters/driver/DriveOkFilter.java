package com.unifun.components.filters.driver;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class DriveOkFilter extends AbstractFilter<DriverStatus> implements DriveFilter {
    @Override
    public AnalyzeResponse<DriverStatus> filter(String text) {
        if (text.contains("logicaldrive") && text.contains("physicaldrive"))
            return new AnalyzeResponse<>(DriverStatus.OK,"");
        return new AnalyzeResponse<>(DriverStatus.UNKNOW, "Cannot parse data, please check manually\n");
    }
}
