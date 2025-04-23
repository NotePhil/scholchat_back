package cmr.notep.business.exceptions;

import cmr.notep.business.exceptions.enums.SchoolErrorCode;

public class SchoolErrorEmail extends SchoolException {
    public SchoolErrorEmail(SchoolErrorCode code, String message) {
        super(code, message);
    }

    public SchoolErrorEmail(SchoolErrorCode code, String message, Throwable cause) {
        super(code, message, cause);
    }
}