package cmr.notep.business.utils;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ExceptionUtil {
    private ExceptionUtil() {
    }

    public static HttpStatus mapSchoolExceptionToHttpStatus(SchoolErrorCode code) {
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return switch (code) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case OPERATION_INTERDITE -> HttpStatus.FORBIDDEN;
            case INTERFACE_NON_RESPECTEE -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    public static ResponseStatusException toResponseStatusException(SchoolException e) {
        return new ResponseStatusException(
                mapSchoolExceptionToHttpStatus(e.getCode()),
                e.getMessage(),
                e
        );
    }
}
