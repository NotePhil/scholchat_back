package cmr.notep.business.exceptions;

import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.ErrorMonitoringService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final ErrorMonitoringService errorMonitoringService;

    public GlobalExceptionHandler(ErrorMonitoringService errorMonitoringService) {
        this.errorMonitoringService = errorMonitoringService;
    }

    @ExceptionHandler(SchoolException.class)
    public ResponseEntity<Object> handleSchoolException(SchoolException ex, WebRequest request) {
        HttpStatus status = switch (ex.getCode()) {
            case INVALID_STATE, INACTIVE_USER -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case OPERATION_INTERDITE -> HttpStatus.FORBIDDEN;
            case INTERFACE_NON_RESPECTEE -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return new ResponseEntity<>(
                new ErrorResponse(ex.getCode(), ex.getMessage()),
                status
        );
    }
    @ExceptionHandler(SchoolErrorEmail.class)
    public ResponseEntity<Object> handleEmailError(SchoolErrorEmail ex, WebRequest request) {
        // Log and monitor the error
        errorMonitoringService.logEmailError(ex);

        return new ResponseEntity<>(
                new ErrorResponse(ex.getCode(), ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
    private HttpStatus mapSchoolExceptionToHttpStatus(SchoolErrorCode code) {
        return switch (code) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case OPERATION_INTERDITE -> HttpStatus.FORBIDDEN;
            case INTERFACE_NON_RESPECTEE -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    // Inner class for structured error responses
    private static class ErrorResponse {
        private final String code;
        private final String message;

        public ErrorResponse(SchoolErrorCode code, String message) {
            this.code = code.name();
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
