package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Un profil (rôle) d'un compte et son état, pour la page « Mes profils » :
 * GET /utilisateurs/{id}/profils, champ {@code profils} de AuthResponse (login / switch-role) et de
 * GET /utilisateurs/{id} (soi-même ou admin).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleProfil implements Serializable {

    /** Profil actif et utilisable (on peut basculer vers lui). */
    public static final String STATUT_ACTIF = "ACTIF";
    /** Profil professeur : pièces déposées, en attente de validation par l'administrateur. */
    public static final String STATUT_EN_ATTENTE_VALIDATION = "EN_ATTENTE_VALIDATION";
    /** Profil professeur : pièces justificatives (CNI recto/verso + selfie) manquantes. */
    public static final String STATUT_DOCUMENTS_MANQUANTS = "DOCUMENTS_MANQUANTS";
    /** Demande refusée (professeur : pièces refusées ; élève : demande d'accès à la classe refusée). */
    public static final String STATUT_REFUSE = "REFUSE";
    /** Profil élève : demande d'accès à la classe en attente d'approbation par le responsable de la classe. */
    public static final String STATUT_EN_ATTENTE_APPROBATION_CLASSE = "EN_ATTENTE_APPROBATION_CLASSE";

    /** PROFESSOR, PARENT, STUDENT, TUTOR, GESTIONNAIRE, ADMIN. */
    private String role;
    /** Vrai si le rôle est actif (connexion / bascule possible). */
    private boolean actif;
    /** Voir les constantes STATUT_* ci-dessus. */
    private String statut;
    /** Profil professeur uniquement : DOCUMENTS_MANQUANTS, EN_ATTENTE_VALIDATION, VALIDE ou REJETE. */
    private String statutVerification;
    /** Motif du refus (professeur refusé, ou demande d'accès élève refusée). */
    private String motifRejet;
    /** Profil élève en attente / refusé : classe de la demande d'accès. */
    private String classeId;
    private String classeNom;
    private LocalDateTime dateAttribution;
}
