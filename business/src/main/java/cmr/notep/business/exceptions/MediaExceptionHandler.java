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
        if (ex.getEnfantIndex() != null) {
            // Inscription parent : index (0-based) de l'enfant concerné dans la liste envoyée
            body.put("enfantIndex", ex.getEnfantIndex());
        }

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
            case NOT_FOUND:
            case RESOURCE_NOT_FOUND:
            case CONTRAT_INTROUVABLE: // "no contract yet" — clients show an empty state on 404
            case CODE_CLASSE_INVALIDE: // inscription : aucun code de classe correspondant
                return HttpStatus.NOT_FOUND;
            case INVALID_INPUT:
            case INVALID_TOKEN:
            // Inscription parent / élève avec code de classe
            case CODE_CLASSE_REQUIS:
            case CLASSE_NON_ACTIVE:
            case CLASSE_RESERVEE_MINEURS:
            // Vérification du compte par code e-mail
            case CODE_VERIFICATION_INVALIDE:
            case CODE_VERIFICATION_EXPIRE:
            case COMPTE_NON_ELIGIBLE:
            // Inscription parent avec ses enfants
            case ENFANTS_REQUIS:
            case ENFANTS_TROP_NOMBREUX:
            case ENFANT_INVALIDE:
            case ENFANT_EN_DOUBLE:
            // Exercice programmé : cours non programmé dans la classe
            case COURS_NON_PROGRAMME_DANS_CLASSE:
                return HttpStatus.BAD_REQUEST;
            case TROP_DE_TENTATIVES:
                return HttpStatus.TOO_MANY_REQUESTS;
            case UNAUTHORIZED:
                return HttpStatus.UNAUTHORIZED;
            case FORBIDDEN:
                return HttpStatus.FORBIDDEN;
            case DUPLICATE_RESOURCE:
            case ALREADY_EXISTS:
            case CONFLICT:
            case ROLE_INCOMPATIBLE:
            case INSCRIPTION_EN_ATTENTE:
            case EMAIL_DEJA_UTILISE:
            case COMPTE_NON_ACTIVE:
            case COMPTE_EN_ATTENTE_VALIDATION:
                return HttpStatus.CONFLICT;
            case OPERATION_INTERDITE:
            case PROFIL_PROFESSEUR_NON_VALIDE:
            // Même correspondance que GlobalExceptionHandler (ce handler-ci l'emporte pour SchoolException)
            case INVALID_STATE:
            case INACTIVE_USER:
            case COMPTE_EN_ATTENTE_APPROBATION:
            case MOT_DE_PASSE_A_CHANGER:
            case CHANGEMENT_PROFIL_INTERDIT_ELEVE:
            case PARENT_SANS_ENFANT_VALIDE:
                return HttpStatus.FORBIDDEN;
            case OPERATION_FAILURE:
            case INIT_ERROR:
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}