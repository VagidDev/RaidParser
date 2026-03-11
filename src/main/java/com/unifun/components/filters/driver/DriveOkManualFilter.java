package com.unifun.components.filters.driver;

import com.unifun.components.filters.AbstractFilter;
import com.unifun.components.response.AnalyzeResponse;

public class DriveOkManualFilter extends AbstractFilter<DriverStatus> implements DriveFilter {
    @Override
    public AnalyzeResponse<DriverStatus> filter(String text) {
        if (text.contains("blocks super") && text.contains("active raid1") && text.contains("[2/2] [UU]")) {
            return new AnalyzeResponse<>(DriverStatus.OK, "");
        }
        return new AnalyzeResponse<>(DriverStatus.UNKNOW, "Unknown RAID status! Please chack manually\n");
    }
}
