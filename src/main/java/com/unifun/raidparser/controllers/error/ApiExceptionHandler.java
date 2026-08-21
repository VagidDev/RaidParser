package com.unifun.raidparser.controllers.error;

import com.unifun.raidparser.config.Profiles;
import com.unifun.raidparser.service.AnalysisAlreadyRunningException;
import com.unifun.raidparser.service.ReportNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Clock;

@RestControllerAdvice
@Profile(Profiles.SERVER)
@RequiredArgsConstructor
public class ApiExceptionHandler {
    private static final Logger LOGGER = LogManager.getLogger(ApiExceptionHandler.class);

    private final Clock clock;

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "not_found", exception.getMessage());
    }

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ApiError> handleReportNotFound(ReportNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "report_not_found", exception.getMessage());
    }

    @ExceptionHandler(AnalysisAlreadyRunningException.class)
    public ResponseEntity<ApiError> handleAlreadyRunning(AnalysisAlreadyRunningException exception) {
        return error(HttpStatus.CONFLICT, "analysis_already_running", exception.getMessage());
    }

    /**
     * Неверное значение параметра, например component=nosuch или date=вчера.
     * Spring оборачивает исключение конвертера, поэтому вытаскиваем исходный
     * текст: именно в нём перечислены допустимые значения.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST, "bad_request",
                "Parameter `" + exception.getName() + "`: " + rootCauseMessage(exception));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, "bad_request", exception.getMessage());
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected error while handling request: {}", exception.getMessage(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", exception.getMessage());
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(code, message, clock.instant()));
    }
}
