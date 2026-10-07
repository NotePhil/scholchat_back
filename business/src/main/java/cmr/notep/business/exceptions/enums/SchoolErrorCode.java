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

    // Combinaison de profils interdite (ex. élève mineur géré par un parent, sans identifiants) — HTTP 409
    ROLE_INCOMPATIBLE("Combinaison de profils non autorisee"),

    // Inscription parent / élève majeur avec code de classe
    CODE_CLASSE_REQUIS("Le code de la classe est obligatoire"),                 // HTTP 400
    CODE_CLASSE_INVALIDE("Aucune classe ne correspond a ce code"),              // HTTP 404
    CLASSE_NON_ACTIVE("Cette classe n'accepte pas d'inscription"),              // HTTP 400
    CLASSE_RESERVEE_MINEURS("Cette classe est reservee aux mineurs"),          // HTTP 400
    INSCRIPTION_EN_ATTENTE("Inscription en attente d'approbation"),             // HTTP 409 (nouvelle inscription refusée)
    COMPTE_EN_ATTENTE_APPROBATION("Compte en attente d'approbation"),            // HTTP 403 (connexion)
    // Mot de passe temporaire à remplacer avant toute autre action — HTTP 403
    MOT_DE_PASSE_A_CHANGER("Vous devez choisir un nouveau mot de passe"),
    // Session ouverte avec le profil élève : pas de changement de profil ni d'ajout de profil — HTTP 403
    CHANGEMENT_PROFIL_INTERDIT_ELEVE("Changement de profil impossible depuis le profil eleve"),
    // Limitation de débit (aperçu de classe, code de vérification…) — HTTP 429
    TROP_DE_TENTATIVES("Trop de tentatives, reessayez plus tard"),
    // Vérification du compte par code envoyé par e-mail — HTTP 400
    CODE_VERIFICATION_INVALIDE("Code de verification invalide"),
    CODE_VERIFICATION_EXPIRE("Code de verification expire"),
    COMPTE_NON_ELIGIBLE("Ce compte ne peut pas etre verifie pour le moment"),
    // Inscription publique avec l'e-mail d'un compte actif, sans session de ce compte — HTTP 409
    EMAIL_DEJA_UTILISE("Un compte existe deja avec cet e-mail"),
    // Inscription publique (anonyme) avec l'e-mail d'un compte NON actif : refusée sans rien modifier — HTTP 409
    COMPTE_NON_ACTIVE("Un compte non active existe deja avec cet e-mail"),
    COMPTE_EN_ATTENTE_VALIDATION("Un compte en attente de validation existe deja avec cet e-mail");
    private final String message;

    SchoolErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}