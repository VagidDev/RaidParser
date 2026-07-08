package com.unifun.raidparser.core.dispatcher;

import com.unifun.raidparser.core.analyzer.Analyzer;
import com.unifun.raidparser.core.analyzer.battery.HpeBatteryAnalyzer;
import com.unifun.raidparser.core.analyzer.drive.HpeDriveAnalyzer;
import com.unifun.raidparser.core.analyzer.drive.MdadmDriveAnalyzer;
import com.unifun.raidparser.core.analyzer.psu.IpmitoolPowerSupplyAnalyzer;
import com.unifun.raidparser.core.filters.Status;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.dto.ServerData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AnalyzeDataDispatcher {
    private static final List<Analyzer<DriverStatus>> DRIVE_ANALYZERS = List.of(
            new HpeDriveAnalyzer(),
            new MdadmDriveAnalyzer()
    );
    private static final List<Analyzer<PowerSupplyStatus>> PSU_ANALYZERS = List.of(
            new IpmitoolPowerSupplyAnalyzer()
    );
    private static final List<Analyzer<BatteryStatus>> BATTERY_ANALYZERS = List.of(
            new HpeBatteryAnalyzer()
    );

    public Analyzer<Status> analyzer(ServerData serverData) {
        //TODO need to refactor server status so it could store status for all components in single objects
        return null;
    }

}
