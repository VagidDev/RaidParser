package com.unifun.raidparser.parser;

import com.unifun.raidparser.core.analyzer.drive.MdadmDriveAnalyzer;
import com.unifun.raidparser.core.component.HealthType;
import com.unifun.raidparser.core.filters.drive.DriverStatus;
import com.unifun.raidparser.dto.ServerData;
import com.unifun.raidparser.dto.ServerStatus;
import com.unifun.raidparser.service.ServerHealthCheckService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RaidStatusParserManualDriverTest {
    private final MdadmDriveAnalyzer mdadmDriveAnalyzer = new MdadmDriveAnalyzer();
    private final RaidStatusParser<DriverStatus> driverStatusRaidStatusParser = new RaidStatusParser<>();

    @Mock private MdadmDriveAnalyzer mockedMdadmDriveAnalyzer;
    @Mock private ServerHealthCheckService serverHealthCheckService;
    // ------------------------------------------------------------------ //
    // 1. Пустой список от checkServers() → пустой результат, analyze не вызван
    // ------------------------------------------------------------------ //
    @Test
    void getManualDriverStatus_emptyServerList_returnsEmptyList() {
        when(serverHealthCheckService.checkServers()).thenReturn(List.of());
        List<ServerStatus<DriverStatus>> result = driverStatusRaidStatusParser.getParsedData(
                serverHealthCheckService.checkServers(),
                mockedMdadmDriveAnalyzer
        );

        assertThat(result).isEmpty();
        verifyNoInteractions(mockedMdadmDriveAnalyzer);
    }

    // ------------------------------------------------------------------ //
    // 2. Один сервер, RAID в норме → DriverStatus.OK
    // ------------------------------------------------------------------ //
    @Test
    void getManualDriverStatus_singleOkServer_returnsOkStatus() {
        String healthData = "Personalities : [raid1]\n"
                + "md0 : active raid1 sda1[0] sdb1[1]\n"
                + "      1953382400 blocks super 1.2 [2/2] [UU]\n";

        ServerData server = new ServerData("host-ok", Map.of(HealthType.DRIVE_HEALTH, healthData));

        List<ServerStatus<DriverStatus>> result = driverStatusRaidStatusParser.getParsedData(List.of(server), mdadmDriveAnalyzer);

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

        ServerData server = new ServerData("host-degraded", Map.of(HealthType.DRIVE_HEALTH, healthData));

        List<ServerStatus<DriverStatus>> result = driverStatusRaidStatusParser.getParsedData(List.of(server), mdadmDriveAnalyzer);

        assertThat(result).hasSize(1);
        ServerStatus<DriverStatus> status = result.get(0);
        assertThat(status.serverName()).isEqualTo("host-degraded");
        assertThat(status.analyzeResponse().getStatus())
                .isEqualTo(DriverStatus.INTERIM_RECOVERY_MODE);
        assertThat(status.analyzeResponse().getErrorText()).contains("super 1.2 [2/1] [U_]");
    }

    // ------------------------------------------------------------------ //
    // 4. Несколько серверов с разными статусами — маппинг serverName сохранён
    // ------------------------------------------------------------------ //
    @Test
    void getManualDriverStatus_multipleServers_eachMappedCorrectly() {
        String okData = "active raid1 blocks super [2/2] [UU]";
        String badData1 = "active raid1 blocks super [1/2] [_U]";
        String badData2 = "active raid1 blocks super [1/2] [U_]";

        List<ServerData> servers = List.of(
                new ServerData("host-a", Map.of(HealthType.DRIVE_HEALTH, okData)),
                new ServerData("host-b", Map.of(HealthType.DRIVE_HEALTH, badData1)),
                new ServerData("host-c", Map.of(HealthType.DRIVE_HEALTH, badData2))
        );

        List<ServerStatus<DriverStatus>> result =driverStatusRaidStatusParser.getParsedData(servers, mdadmDriveAnalyzer);

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
    void getManualDriverStatus_analyzeCalledOncePerServer_inOrder() {
        String dataA = "data-a";
        String dataB = "data-b";

        List<ServerData> servers = List.of(
                new ServerData("srv-1", Map.of(HealthType.DRIVE_HEALTH, dataA)),
                new ServerData("srv-2", Map.of(HealthType.DRIVE_HEALTH, dataB))
        );
        when(mockedMdadmDriveAnalyzer.getSupportedType()).thenReturn(HealthType.DRIVE_HEALTH);

        driverStatusRaidStatusParser.getParsedData(servers, mockedMdadmDriveAnalyzer);

        // InOrder гарантирует последовательность вызовов
        var inOrder = inOrder(mockedMdadmDriveAnalyzer);
        inOrder.verify(mockedMdadmDriveAnalyzer).analyze(dataA);
        inOrder.verify(mockedMdadmDriveAnalyzer).analyze(dataB);
        verifyNoMoreInteractions(mockedMdadmDriveAnalyzer);
    }
}
