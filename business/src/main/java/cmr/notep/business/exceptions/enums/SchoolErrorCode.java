package cmr.notep.business.exceptions.enums;

public enum SchoolErrorCode {
    // Existing codes
    NOT_FOUND("Resource not found"),
    OPERATION_INTERDITE("Operation is not allowed"),
    INTERFACE_NON_RESPECTEE("Interface contract not respected"),
    INTERNAL_ERROR("Internal server error"),
    INVALID_INPUT("Invalid input data"),
    INVALID_TOKEN("Invalid activation token"),
    INVALID_OPERATION("Invalid operation"),
    EMAIL_NOT_SENT("The activation email could not be sent."),
    INVALID_STATE("The user is not in a valid state for this operation."),
    MAPPING_FAILED("User entity mapping failed"),
    TOKEN_EXPIRED("Token Expired"),
    INACTIVE_USER("Cannot create refresh token for inactive user"),
    EMAIL_ERROR("Email processing error"),

    // New codes needed for MediaExceptionHandler
    RESOURCE_NOT_FOUND("Resource not found"),
    UNAUTHORIZED("Unauthorized access"),
    FORBIDDEN("Access forbidden"),
    DUPLICATE_RESOURCE("Resource already exists"),
    OPERATION_FAILURE("Operation failed"),
    INIT_ERROR("Initialization error"),
    ALREADY_EXISTS("Existing entity"),
    INVALID_CODE("Code d'activation invalide"),
    CONFLICT("Utilisateur existe deja"),
    PAYMENT_FAILED("Payment processing failed"),

    // Codes pour le module Offres/Contrats
    OFFRE_INTROUVABLE("Offre introuvable"),
    CONTRAT_INTROUVABLE("Contrat introuvable"),
    CIBLE_OFFRE_INVALIDE("Cette offre ne correspond pas au type de cible demande (classe/etablissement)"),
    QUOTA_CLASSES_ATTEINT("Le quota de classes de votre forfait est atteint"),
    ABONNEMENT_EXPIRE("Votre offre a expire, veuillez la renouveler pour continuer"),
    RENOUVELLEMENT_TOKEN_INVALIDE("Lien de renouvellement invalide ou expire"),

    // Professeur dont les pièces justificatives ne sont pas (encore) validées par l'administrateur — HTTP 403
    PROFIL_PROFESSEUR_NON_VALIDE("Votre profil professeur n'est pas encore valide par l'administrateur"),

    // Combinaison de profils interdite (un compte élève ne peut détenir aucun autre profil) — HTTP 409
    ROLE_INCOMPATIBLE("Combinaison de profils non autorisee");
    private final String message;

    SchoolErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}