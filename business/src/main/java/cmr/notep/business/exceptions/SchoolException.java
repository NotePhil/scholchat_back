package cmr.notep.business.exceptions;

import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import lombok.Getter;

@Getter
public class SchoolException extends RuntimeException {
    private final SchoolErrorCode code;
    /** Erreur portant sur un enfant de la demande (inscription parent) : index 0-based dans la liste, sinon null. */
    private Integer enfantIndex;

    public SchoolException(SchoolErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public SchoolException(SchoolErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /** Rattache l'erreur à l'enfant d'index donné (0-based) de la liste envoyée. */
    public SchoolException avecEnfantIndex(Integer index) {
        this.enfantIndex = index;
        return this;
    }

    public SchoolErrorCode getCode() {
        return code;
    }
}
