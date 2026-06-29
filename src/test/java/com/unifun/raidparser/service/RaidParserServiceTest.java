package com.unifun.raidparser.service;

import com.unifun.raidparser.core.analyzer.BatteryAnalyzer;
import com.unifun.raidparser.core.analyzer.DriveAnalyzer;
import com.unifun.raidparser.core.analyzer.DriveManualAnalyzer;
import com.unifun.raidparser.core.analyzer.PowerSupplyAnalyzer;
import com.unifun.raidparser.core.filters.battery.BatteryStatus;
import com.unifun.raidparser.core.filters.driver.DriverStatus;
import com.unifun.raidparser.core.filters.power.PowerSupplyStatus;
import com.unifun.raidparser.core.response.AnalyzeResponse;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.handlers.ServerDataHandler;
import com.unifun.raidparser.parser.RaidStatusParser;
import com.unifun.raidparser.util.ServerDataSorter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RaidParserServiceManualDriverTest {

    // Мокаем только то, что нужно для getManualDriverStatus
    @Mock private ServerHealthCheckService serverHealthCheckService;
    @Mock private DriveManualAnalyzer driveManualAnalyzer;

    // Остальные зависимости — тоже мок, иначе @InjectMocks не соберётся
    @Mock private ServerDataHandler serverDataHandler;
    @Mock private DriveAnalyzer driveAnalyzer;
    @Mock private PowerSupplyAnalyzer powerSupplyAnalyzer;
    @Mock private BatteryAnalyzer batteryAnalyzer;
    @Mock private RaidStatusParser<DriverStatus> driverStatusRaidParser;
    @Mock private RaidStatusParser<PowerSupplyStatus> powerSupplyStatusRaidParser;
    @Mock private RaidStatusParser<BatteryStatus> batteryStatusRaidParser;
    @Mock private ServerDataSorter<DriverStatus> driverStatusDataSorter;
    @Mock private ServerDataSorter<PowerSupplyStatus> powerSupplyStatusDataSorter;
    @Mock private ServerDataSorter<BatteryStatus> batteryStatusDataSorter;

    @InjectMocks
    private RaidParserService service;

    // ------------------------------------------------------------------ //
    // 1. Пустой список от checkServers() → пустой результат, analyze не вызван
    // ------------------------------------------------------------------ //
    @Test
    void getManualDriverStatus_emptyServerList_returnsEmptyList() {
        when(serverHealthCheckService.checkServers()).thenReturn(List.of());

        List<ServerStatus<DriverStatus>> result = service.getManualDriverStatus();

        assertThat(result).isEmpty();
        verifyNoInteractions(driveManualAnalyzer);
    }

    // ------------------------------------------------------------------ //
    // 2. Один сервер, RAID в норме → DriverStatus.OK
    // ------------------------------------------------------------------ //
    @Test
    void getManualDriverStatus_singleOkServer_returnsOkStatus() {
        String healthData = "Personalities : [raid1]\n"
                + "md0 : active raid1 sda1[0] sdb1[1]\n"
                + "      1953382400 blocks super 1.2 [2/2] [UU]\n";

        ServerData server = new ServerData("host-ok", healthData);
        AnalyzeResponse<DriverStatus> analyzeResponse =
                new AnalyzeResponse<>(DriverStatus.OK, "");

        when(serverHealthCheckService.checkServers()).thenReturn(List.of(server));
        when(driveManualAnalyzer.analyze(healthData)).thenReturn(analyzeResponse);

        List<ServerStatus<DriverStatus>> result = service.getManualDriverStatus();

        assertThat(result).hasSize(1);
        ServerStatus<DriverStatus> status = result.get(0);
        assertThat(status.serverName()).isEqualTo("host-ok");
        assertThat(status.analyzeResponse().getStatus()).isEqualTo(DriverStatus.OK);
        assertThat(status.analyzeResponse().getErrorText()).isEmpty();
    }

    // ------------------------------------------------------------------ //
    // 3. Один сервер, RAID деградирован → INTERIM_RECOVERY_MODE
    // ------------------------------------------------------------------ //
    @Test
    void getManualDriverStatus_singleDegradedServer_returnsInterimRecoveryMode() {
        String healthData = "Personalities : [raid1]\n"
                + "md0 : active raid1 sda1[0] sdb1[1](F)\n"
                + "      1953382400 blocks super 1.2 [2/1] [U_]\n";

        ServerData server = new ServerData("host-degraded", healthData);
        AnalyzeResponse<DriverStatus> analyzeResponse =
                new AnalyzeResponse<>(DriverStatus.INTERIM_RECOVERY_MODE, "md0 degraded");

        when(serverHealthCheckService.checkServers()).thenReturn(List.of(server));
        when(driveManualAnalyzer.analyze(healthData)).thenReturn(analyzeResponse);

        List<ServerStatus<DriverStatus>> result = service.getManualDriverStatus();

        assertThat(result).hasSize(1);
        ServerStatus<DriverStatus> status = result.get(0);
        assertThat(status.serverName()).isEqualTo("host-degraded");
        assertThat(status.analyzeResponse().getStatus())
                .isEqualTo(DriverStatus.INTERIM_RECOVERY_MODE);
        assertThat(status.analyzeResponse().getErrorText()).contains("md0 degraded");
    }

    // ------------------------------------------------------------------ //
    // 4. Несколько серверов с разными статусами — маппинг serverName сохранён
    // ------------------------------------------------------------------ //
    @Test
    void getManualDriverStatus_multipleServers_eachMappedCorrectly() {
        String okData   = "active raid1 blocks super [2/2] [UU]";
        String badData1 = "active raid1 blocks super [1/2] [_U]";
        String badData2 = "active raid1 blocks super [1/2] [U_]";

        List<ServerData> servers = List.of(
                new ServerData("host-a", okData),
                new ServerData("host-b", badData1),
                new ServerData("host-c", badData2)
        );

        when(serverHealthCheckService.checkServers()).thenReturn(servers);
        when(driveManualAnalyzer.analyze(okData))
                .thenReturn(new AnalyzeResponse<>(DriverStatus.OK, ""));
        when(driveManualAnalyzer.analyze(badData1))
                .thenReturn(new AnalyzeResponse<>(DriverStatus.INTERIM_RECOVERY_MODE, "host-b fail"));
        when(driveManualAnalyzer.analyze(badData2))
                .thenReturn(new AnalyzeResponse<>(DriverStatus.INTERIM_RECOVERY_MODE, "host-c fail"));

        List<ServerStatus<DriverStatus>> result = service.getManualDriverStatus();

        assertThat(result).hasSize(3);

        assertThat(result.get(0).serverName()).isEqualTo("host-a");
        assertThat(result.get(0).analyzeResponse().getStatus()).isEqualTo(DriverStatus.OK);

        assertThat(result.get(1).serverName()).isEqualTo("host-b");
        assertThat(result.get(1).analyzeResponse().getStatus())
                .isEqualTo(DriverStatus.INTERIM_RECOVERY_MODE);

        assertThat(result.get(2).serverName()).isEqualTo("host-c");
        assertThat(result.get(2).analyzeResponse().getStatus())
                .isEqualTo(DriverStatus.INTERIM_RECOVERY_MODE);
    }

    // ------------------------------------------------------------------ //
    // 5. Verify: каждый healthData передан в analyze ровно один раз,
    //    порядок соответствует порядку от checkServers()
    // ------------------------------------------------------------------ //
    @Test
    void getManualDriverStatus_analyzCalledOncePerServer_inOrder() {
        String dataA = "data-a";
        String dataB = "data-b";

        when(serverHealthCheckService.checkServers()).thenReturn(List.of(
                new ServerData("srv-1", dataA),
                new ServerData("srv-2", dataB)
        ));
        when(driveManualAnalyzer.analyze(any()))
                .thenReturn(new AnalyzeResponse<>(DriverStatus.OK, ""));

        service.getManualDriverStatus();

        // InOrder гарантирует последовательность вызовов
        var inOrder = inOrder(driveManualAnalyzer);
        inOrder.verify(driveManualAnalyzer).analyze(dataA);
        inOrder.verify(driveManualAnalyzer).analyze(dataB);
        verifyNoMoreInteractions(driveManualAnalyzer);
    }
}