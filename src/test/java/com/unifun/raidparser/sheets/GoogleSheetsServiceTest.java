package com.unifun.raidparser.sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.unifun.raidparser.dto.ReportServerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit-тесты для {@link GoogleSheetsService}.
 *
 * GoogleSheetsService принимает Sheets через поле (нет конструктора с Sheets),
 * поэтому sheetsService инжектируется через ReflectionTestUtils.
 * Все остальные зависимости не нужны — upload() их не использует.
 */
@ExtendWith(MockitoExtension.class)
class GoogleSheetsServiceTest {

    // Цепочка моков Google Sheets API:
    // sheetsService.spreadsheets().values().update(...).setValueInputOption(...).execute()
    @Mock private Sheets sheetsService;
    @Mock private Sheets.Spreadsheets spreadsheets;
    @Mock private Sheets.Spreadsheets.Values values;
    @Mock private Sheets.Spreadsheets.Values.Update updateRequest;

    private GoogleSheetsService service;

    private static final String SPREADSHEET_ID = "test-spreadsheet-id-123";
    private static final String RANGE           = "Sheet1!A1:C10";

    // ── Фабричные методы для тестовых данных ─────────────────────────────────

    private ReportServerData healthyServer() {
        return new ReportServerData("server-01", "OK", "");
    }

    private ReportServerData unhealthyServer() {
        return new ReportServerData("server-02", "ERROR", "Disk full");
    }

    private ReportServerData serverWithWhitespace() {
        return new ReportServerData("server-03", "  WARNING  ", "  low memory  ");
    }

    // ── setUp ─────────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() {
        service = new GoogleSheetsService();
        // Sheets инжектируем через рефлексию — у сервиса нет конструктора с Sheets
        ReflectionTestUtils.setField(service, "sheetsService", sheetsService);
    }

    // Вспомогательный метод: настройка цепочки моков (только там где нужно)
    private void setupSheetsMock() throws IOException {
        when(sheetsService.spreadsheets()).thenReturn(spreadsheets);
        when(spreadsheets.values()).thenReturn(values);
        when(values.update(anyString(), anyString(), any(ValueRange.class))).thenReturn(updateRequest);
        when(updateRequest.setValueInputOption(anyString())).thenReturn(updateRequest);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  upload() — happy path
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("upload() — happy path")
    class UploadHappyPath {

        @Test
        @DisplayName("Одна запись — данные отправляются корректно")
        void singleRecord_sendsCorrectValues() throws IOException {
            setupSheetsMock();
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            service.upload(SPREADSHEET_ID, RANGE, List.of(healthyServer()));

            ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
            verify(values).update(eq(SPREADSHEET_ID), eq(RANGE), captor.capture());

            List<List<Object>> sent = captor.getValue().getValues();
            assertThat(sent).hasSize(1);
            assertThat(sent.get(0)).containsExactly("server-01", "OK", "");
        }

        @Test
        @DisplayName("Несколько записей — все строки попадают в таблицу")
        void multipleRecords_allRowsSent() throws IOException {
            setupSheetsMock();
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            service.upload(SPREADSHEET_ID, RANGE, List.of(healthyServer(), unhealthyServer()));

            ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
            verify(values).update(eq(SPREADSHEET_ID), eq(RANGE), captor.capture());

            List<List<Object>> sent = captor.getValue().getValues();
            assertThat(sent).hasSize(2);
            assertThat(sent.get(0)).containsExactly("server-01", "OK", "");
            assertThat(sent.get(1)).containsExactly("server-02", "ERROR", "Disk full");
        }

        @Test
        @DisplayName("Пробелы в healthStatus и errorText обрезаются через trim()")
        void whitespaceInFields_isTrimmed() throws IOException {
            setupSheetsMock();
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            service.upload(SPREADSHEET_ID, RANGE, List.of(serverWithWhitespace()));

            ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
            verify(values).update(anyString(), anyString(), captor.capture());

            List<Object> row = captor.getValue().getValues().get(0);
            assertThat(row.get(1)).isEqualTo("WARNING");
            assertThat(row.get(2)).isEqualTo("low memory");
        }

        @Test
        @DisplayName("ValueInputOption всегда RAW")
        void valueInputOption_isRaw() throws IOException {
            setupSheetsMock();
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            service.upload(SPREADSHEET_ID, RANGE, List.of(healthyServer()));

            verify(updateRequest).setValueInputOption("RAW");
        }

        @Test
        @DisplayName("execute() вызывается ровно один раз")
        void execute_calledExactlyOnce() throws IOException {
            setupSheetsMock();
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            service.upload(SPREADSHEET_ID, RANGE, List.of(healthyServer(), unhealthyServer()));

            verify(updateRequest, times(1)).execute();
        }

        @Test
        @DisplayName("Пустой список данных — в API передаётся пустой ValueRange")
        void emptyDataList_sendsEmptyValueRange() throws IOException {
            setupSheetsMock();
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            service.upload(SPREADSHEET_ID, RANGE, Collections.emptyList());

            ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
            verify(values).update(eq(SPREADSHEET_ID), eq(RANGE), captor.capture());
            assertThat(captor.getValue().getValues()).isEmpty();
        }

        @Test
        @DisplayName("Пустые строки в полях DTO — передаются как пустые строки, не null")
        void emptyStringFields_sentAsEmptyNotNull() throws IOException {
            setupSheetsMock();
            when(updateRequest.execute()).thenReturn(new UpdateValuesResponse());

            service.upload(SPREADSHEET_ID, RANGE,
                    List.of(new ReportServerData("server-X", "", "")));

            ArgumentCaptor<ValueRange> captor = ArgumentCaptor.forClass(ValueRange.class);
            verify(values).update(anyString(), anyString(), captor.capture());

            List<Object> row = captor.getValue().getValues().get(0);
            assertThat(row).doesNotContainNull();
            assertThat(row).containsExactly("server-X", "", "");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  upload() — guard clauses (ранний выход при некорректных аргументах)
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("upload() — guard clauses")
    class UploadGuardClauses {

        @Test
        @DisplayName("Пустой spreadsheetId — API не вызывается")
        void emptySpreadsheetId_doesNotCallApi() throws IOException {
            service.upload("", RANGE, List.of(healthyServer()));
            verifyNoInteractions(sheetsService);
        }

        @Test
        @DisplayName("Null spreadsheetId — API не вызывается")
        void nullSpreadsheetId_doesNotCallApi() throws IOException {
            service.upload(null, RANGE, List.of(healthyServer()));
            verifyNoInteractions(sheetsService);
        }

        @Test
        @DisplayName("Пустой range — API не вызывается")
        void emptyRange_doesNotCallApi() throws IOException {
            service.upload(SPREADSHEET_ID, "", List.of(healthyServer()));
            verifyNoInteractions(sheetsService);
        }

        @Test
        @DisplayName("Null range — API не вызывается")
        void nullRange_doesNotCallApi() throws IOException {
            service.upload(SPREADSHEET_ID, null, List.of(healthyServer()));
            verifyNoInteractions(sheetsService);
        }

        @Test
        @DisplayName("Null список данных — API не вызывается")
        void nullDataList_doesNotCallApi() throws IOException {
            service.upload(SPREADSHEET_ID, RANGE, null);
            verifyNoInteractions(sheetsService);
        }

        @Test
        @DisplayName("Пробельный spreadsheetId — API не вызывается")
        void blankSpreadsheetId_doesNotCallApi() throws IOException {
            service.upload("   ", RANGE, List.of(healthyServer()));
            verifyNoInteractions(sheetsService);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  upload() — exceptions
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("upload() — exceptions")
    class UploadExceptions {

        @Test
        @DisplayName("IOException от execute() пробрасывается наверх")
        void ioExceptionFromExecute_isPropagated() throws IOException {
            setupSheetsMock();
            when(updateRequest.execute()).thenThrow(new IOException("Network error"));

            assertThatThrownBy(() -> service.upload(SPREADSHEET_ID, RANGE, List.of(healthyServer())))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Network error");
        }

        @Test
        @DisplayName("RuntimeException от execute() пробрасывается наверх")
        void runtimeExceptionFromExecute_isPropagated() throws IOException {
            setupSheetsMock();
            when(updateRequest.execute()).thenThrow(new RuntimeException("Unexpected error"));

            assertThatThrownBy(() -> service.upload(SPREADSHEET_ID, RANGE, List.of(healthyServer())))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Unexpected error");
        }

        @Test
        @DisplayName("IOException при values.update() пробрасывается наверх")
        void ioExceptionFromValuesUpdate_isPropagated() throws IOException {
            setupSheetsMock();
            when(values.update(anyString(), anyString(), any(ValueRange.class)))
                    .thenThrow(new IOException("Sheets API unreachable"));

            assertThatThrownBy(() -> service.upload(SPREADSHEET_ID, RANGE, List.of(healthyServer())))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Sheets API unreachable");
        }
    }
}