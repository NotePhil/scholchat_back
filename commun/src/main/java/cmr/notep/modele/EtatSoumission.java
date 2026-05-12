package cmr.notep.modele;

public enum EtatSoumission {
    EN_COURS,               // Student started but not submitted
    SOUMIS,                 // Submitted, pending auto-correction or manual review
    EN_ATTENTE_CORRECTION,  // Submitted, waiting for professor manual correction
    CORRIGE                 // Professor has corrected and returned the grade
}
