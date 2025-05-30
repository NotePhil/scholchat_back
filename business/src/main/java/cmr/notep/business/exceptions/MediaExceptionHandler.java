package cmr.notep.business.exceptions;

import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class MediaExceptionHandler {

    @ExceptionHandler(SchoolException.class)
    public ResponseEntity<Object> handleSchoolException(SchoolException ex) {
        log.error("SchoolException: {}", ex.getMessage(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());
        body.put("code", ex.getCode().name());

        HttpStatus status = mapErrorCodeToHttpStatus(ex.getCode());

        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", "An unexpected error occurred");
        body.put("error", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus mapErrorCodeToHttpStatus(SchoolErrorCode code) {
        switch (code) {
            case RESOURCE_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case INVALID_INPUT:
            case INVALID_TOKEN:
                return HttpStatus.BAD_REQUEST;
            case UNAUTHORIZED:
                return HttpStatus.UNAUTHORIZED;
            case FORBIDDEN:
                return HttpStatus.FORBIDDEN;
            case DUPLICATE_RESOURCE:
                return HttpStatus.CONFLICT;
            case OPERATION_FAILURE:
            case INIT_ERROR:
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}