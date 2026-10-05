package cmr.notep.modele;

/**
 * Statut de vérification du PROFIL professeur (pièces d'identité contrôlées par l'administrateur),
 * distinct de l'état du compte ({@link EtatUtilisateur}) : un compte peut être ACTIVE (connexion
 * possible, activation "partielle") sans que le profil professeur soit validé. Seul
 * {@link #VALIDE} donne les droits professeur (classes, cours, exercices, sessions…).
 * Persisté dans ressources.professeurs.statut_verification (SQL natif uniquement).
 */
public enum StatutVerificationProfesseur {
    /** Pièces (CNI recto/verso + selfie) incomplètes. */
    DOCUMENTS_MANQUANTS,
    /** Pièces complètes, en attente de la décision de l'administrateur. */
    EN_ATTENTE_VALIDATION,
    /** Pièces vérifiées par l'administrateur : droits professeur accordés. */
    VALIDE,
    /** Pièces refusées (motif fourni) ; un nouveau dépôt renvoie en EN_ATTENTE_VALIDATION. */
    REJETE;

    public static StatutVerificationProfesseur parse(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
