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
        System.out.println("GlobalExceptionHandler - Handling SchoolException");
        HttpStatus status = mapSchoolExceptionToHttpStatus(ex.getCode());

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
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex, WebRequest request) {
        String message = "Une erreur de donnees est survenue / A data error occurred";
        String errorMsg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (errorMsg.contains("email") && errorMsg.contains("unique")) {
            message = "Un compte avec cet email existe deja / An account with this email already exists";
        } else if (errorMsg.contains("unique")) {
            message = "Cette valeur existe deja / This value already exists";
        }

        return new ResponseEntity<>(
                new ErrorResponse(SchoolErrorCode.DUPLICATE_RESOURCE, message),
                HttpStatus.CONFLICT
        );
    }

    private HttpStatus mapSchoolExceptionToHttpStatus(SchoolErrorCode code) {
        return switch (code) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case OPERATION_INTERDITE, INVALID_STATE, INACTIVE_USER -> HttpStatus.FORBIDDEN;
            case INTERFACE_NON_RESPECTEE, INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case DUPLICATE_RESOURCE, ALREADY_EXISTS, CONFLICT -> HttpStatus.CONFLICT;
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
