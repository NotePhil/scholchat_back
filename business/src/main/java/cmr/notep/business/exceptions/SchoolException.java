package cmr.notep.business.exceptions;

import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import lombok.Getter;

@Getter
public class SchoolException extends RuntimeException {
    private final SchoolErrorCode code;

    public SchoolException(SchoolErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public SchoolException(SchoolErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public SchoolErrorCode getCode() {
        return code;
    }
}
