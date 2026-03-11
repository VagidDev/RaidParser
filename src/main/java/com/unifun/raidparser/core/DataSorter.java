package com.unifun.raidparser.core;

import com.unifun.raidparser.analyzer.BatteryAnalyzer;
import com.unifun.raidparser.analyzer.DriveAnalyzer;
import com.unifun.raidparser.analyzer.PowerSupplyAnalyzer;
import com.unifun.raidparser.analyzer.filters.Status;
import com.unifun.raidparser.analyzer.filters.battery.BatteryStatus;
import com.unifun.raidparser.analyzer.filters.driver.DriverStatus;
import com.unifun.raidparser.analyzer.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.analyzer.response.AnalyzeResponse;

import java.util.*;

public class DataSorter<T extends Status> {
    public List<Map.Entry<String, AnalyzeResponse<T>>> sortByStatus(Map<String, AnalyzeResponse<T>> serversStatus) {
        // Сортировка по приоритету
        List<Map.Entry<String, AnalyzeResponse<T>>> sortedServersStatus = new ArrayList<>(serversStatus.entrySet());
        sortedServersStatus.sort(Comparator.comparingInt(entry -> entry.getValue().getStatus().getPriority()));

        return sortedServersStatus;
    }

    public static List<Map.Entry<String, AnalyzeResponse<DriverStatus>>> getSortedDrives(Map<String, String> servers) {
        DriveAnalyzer driveAnalyzer = new DriveAnalyzer();
        Map<String, AnalyzeResponse<DriverStatus>> driveServerStatuses = new HashMap<>();
        for (Map.Entry<String, String> entry : servers.entrySet()) {

            AnalyzeResponse<DriverStatus> driveResponse = driveAnalyzer.analyze(entry.getValue());
            driveServerStatuses.put(entry.getKey(), driveResponse);
        }

        // Сортировка по приоритету
        List<Map.Entry<String, AnalyzeResponse<DriverStatus>>> sortedDriveStatuses = new ArrayList<>(driveServerStatuses.entrySet());
        sortedDriveStatuses.sort(Comparator.comparingInt(entry -> entry.getValue().getStatus().getPriority()));

        return sortedDriveStatuses;
    }

    public static List<Map.Entry<String, AnalyzeResponse<PowerSupplyStatus>>> getSortedPowerSupplies(Map<String, String> servers) {
        PowerSupplyAnalyzer powerSupplyAnalyzer = new PowerSupplyAnalyzer();
        Map<String, AnalyzeResponse<PowerSupplyStatus>> powerSupplyServerStatuses = new HashMap<>();

        for (Map.Entry<String, String> entry : servers.entrySet()) {
            AnalyzeResponse<PowerSupplyStatus> powerSupplyResponse = powerSupplyAnalyzer.analyze(entry.getValue());
            powerSupplyServerStatuses.put(entry.getKey(), powerSupplyResponse);
        }

        // Сортировка по приоритету
        List<Map.Entry<String, AnalyzeResponse<PowerSupplyStatus>>> sortedPowerSupplyStatuses = new ArrayList<>(powerSupplyServerStatuses.entrySet());
        sortedPowerSupplyStatuses.sort(Comparator.comparingInt(entry -> entry.getValue().getStatus().getPriority()));

        return sortedPowerSupplyStatuses;
    }

    public static List<Map.Entry<String, AnalyzeResponse<BatteryStatus>>> getSortedBatteries(Map<String, String> servers) {
        BatteryAnalyzer batteryAnalyzer = new BatteryAnalyzer();
        Map<String, AnalyzeResponse<BatteryStatus>> batteryServerStatuses = new HashMap<>();

        for (Map.Entry<String, String> entry : servers.entrySet()) {
            AnalyzeResponse<BatteryStatus> batteryResponse = batteryAnalyzer.analyze(entry.getValue());
            batteryServerStatuses.put(entry.getKey(), batteryResponse);
        }

        // Сортировка по приоритету
        List<Map.Entry<String, AnalyzeResponse<BatteryStatus>>> sortedBatteryStatuses = new ArrayList<>(batteryServerStatuses.entrySet());
        sortedBatteryStatuses.sort(Comparator.comparingInt(entry -> entry.getValue().getStatus().getPriority()));

        return sortedBatteryStatuses;
    }
}
