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
            case NOT_FOUND, CODE_CLASSE_INVALIDE -> HttpStatus.NOT_FOUND;
            case OPERATION_INTERDITE, FORBIDDEN, INVALID_STATE, INACTIVE_USER, PROFIL_PROFESSEUR_NON_VALIDE,
                 MOT_DE_PASSE_A_CHANGER, COMPTE_EN_ATTENTE_APPROBATION,
                 CHANGEMENT_PROFIL_INTERDIT_ELEVE -> HttpStatus.FORBIDDEN;
            case TROP_DE_TENTATIVES -> HttpStatus.TOO_MANY_REQUESTS;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case INTERFACE_NON_RESPECTEE, INVALID_INPUT, CODE_CLASSE_REQUIS, CLASSE_NON_ACTIVE,
                 CLASSE_RESERVEE_MINEURS, CODE_VERIFICATION_INVALIDE, CODE_VERIFICATION_EXPIRE,
                 COMPTE_NON_ELIGIBLE -> HttpStatus.BAD_REQUEST;
            case DUPLICATE_RESOURCE, ROLE_INCOMPATIBLE, EMAIL_DEJA_UTILISE, COMPTE_NON_ACTIVE,
                 COMPTE_EN_ATTENTE_VALIDATION -> HttpStatus.CONFLICT;
            // Inscription refusée : un compte en attente d'approbation existe déjà pour cet e-mail
            case INSCRIPTION_EN_ATTENTE -> HttpStatus.CONFLICT;
            // Jeton (activation / réinitialisation) invalide ou expiré : erreur client, pas 500
            case INVALID_TOKEN, TOKEN_EXPIRED -> HttpStatus.BAD_REQUEST;
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
