package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.ressourcesjpa.dao.MessagesEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;
import java.util.Date;

public interface UtilisateursRepository extends JpaRepository<UtilisateursEntity, String> {
    Optional<UtilisateursEntity> findByEmail(String email);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM utilisateurs u WHERE u.etat = :etat AND u.creation_date < :creationDate", nativeQuery = true)
    int deleteByEtatAndCreationDateBefore(@Param("etat") String etat, @Param("creationDate") Date creationDate);


    @Query("SELECT u FROM UtilisateursEntity u WHERE TYPE(u) = ProfesseursEntity AND u.etat = :etat")
    List<UtilisateursEntity> findByEtat(@Param("etat") EtatUtilisateur etat);
    @Query("SELECT u FROM UtilisateursEntity u JOIN u.classes c WHERE c.id = :classeId")
    List<UtilisateursEntity> findByClasseId(@Param("classeId") String classeId);

    @Query("SELECT u.id FROM UtilisateursEntity u WHERE u.admin = true")
    List<String> findAdminUserIds();

    List<UtilisateursEntity> findByAdminTrue();

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ressources.parents (parents_id) VALUES (:userId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void insertParentRole(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ressources.eleves (eleves_id, niveau) VALUES (:userId, :niveau) ON CONFLICT DO NOTHING", nativeQuery = true)
    void insertEleveRole(@Param("userId") String userId, @Param("niveau") String niveau);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ressources.professeurs (professeurs_id, has_uploaded) VALUES (:userId, false) ON CONFLICT DO NOTHING", nativeQuery = true)
    void insertProfesseurRole(@Param("userId") String userId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO ressources.gestionnaires (gestionnaires_id) VALUES (:userId) ON CONFLICT DO NOTHING", nativeQuery = true)
    void insertGestionnaireRole(@Param("userId") String userId);

    // ── Comptes multi-rôles ──────────────────────────────────────────────────
    // Un même utilisateur peut avoir une ligne dans plusieurs tables filles (professeurs, parents,
    // eleves…). Hibernate (héritage JOINED) ne charge alors qu'UN sous-type par id et par contexte
    // de persistance : ces requêtes scalaires/natives évitent de dépendre du sous-type chargé.

    /** Id du compte pour un email, sans charger (ni rendre managée) l'entité. */
    @Query("SELECT u.id FROM UtilisateursEntity u WHERE u.email = :email")
    Optional<String> findIdByEmail(@Param("email") String email);

    /** Drapeau "mot de passe temporaire à changer" (lu à chaque requête authentifiée par le filtre JWT). */
    @Query(value = "SELECT COALESCE((SELECT must_change_password FROM ressources.utilisateurs WHERE email = :email), false)",
            nativeQuery = true)
    boolean findMustChangePasswordByEmail(@Param("email") String email);

    @Query(value = "SELECT COUNT(*) > 0 FROM ressources.professeurs WHERE professeurs_id = :userId", nativeQuery = true)
    boolean hasProfesseurRow(@Param("userId") String userId);

    @Query(value = "SELECT COUNT(*) > 0 FROM ressources.parents WHERE parents_id = :userId", nativeQuery = true)
    boolean hasParentRow(@Param("userId") String userId);

    @Query(value = "SELECT COUNT(*) > 0 FROM ressources.eleves WHERE eleves_id = :userId", nativeQuery = true)
    boolean hasEleveRow(@Param("userId") String userId);

    @Query(value = "SELECT COUNT(*) > 0 FROM ressources.repetiteurs WHERE repetiteurs_id = :userId", nativeQuery = true)
    boolean hasRepetiteurRow(@Param("userId") String userId);

    @Query(value = "SELECT COUNT(*) > 0 FROM ressources.gestionnaires WHERE gestionnaires_id = :userId", nativeQuery = true)
    boolean hasGestionnaireRow(@Param("userId") String userId);

    /** Présence dans chaque table fille, en une requête : [professeurs, eleves, repetiteurs, parents, gestionnaires]. */
    @Query(value = "SELECT EXISTS (SELECT 1 FROM ressources.professeurs WHERE professeurs_id = :userId), "
            + "EXISTS (SELECT 1 FROM ressources.eleves WHERE eleves_id = :userId), "
            + "EXISTS (SELECT 1 FROM ressources.repetiteurs WHERE repetiteurs_id = :userId), "
            + "EXISTS (SELECT 1 FROM ressources.parents WHERE parents_id = :userId), "
            + "EXISTS (SELECT 1 FROM ressources.gestionnaires WHERE gestionnaires_id = :userId)", nativeQuery = true)
    List<Object[]> findSubtypeFlags(@Param("userId") String userId);

    @Query(value = "SELECT COALESCE((SELECT has_uploaded FROM ressources.professeurs WHERE professeurs_id = :userId), false)", nativeQuery = true)
    boolean professeurHasUploaded(@Param("userId") String userId);

    /** Pièces du rôle professeur, écrites directement dans la table professeurs (compte multi-rôles). */
    @Transactional
    @Modifying
    @Query(value = "UPDATE ressources.professeurs SET "
            + "cni_url_front = COALESCE(CAST(:recto AS VARCHAR), cni_url_front), "
            + "cni_url_back = COALESCE(CAST(:verso AS VARCHAR), cni_url_back), "
            + "selfie_url = COALESCE(CAST(:selfie AS VARCHAR), selfie_url), "
            + "matricule_professeur = COALESCE(CAST(:matricule AS VARCHAR), matricule_professeur), "
            + "has_uploaded = (COALESCE(CAST(:recto AS VARCHAR), cni_url_front) IS NOT NULL AND COALESCE(CAST(:verso AS VARCHAR), cni_url_back) IS NOT NULL "
            + "AND COALESCE(CAST(:selfie AS VARCHAR), selfie_url) IS NOT NULL) "
            + "WHERE professeurs_id = :userId", nativeQuery = true)
    int updateProfesseurDocuments(@Param("userId") String userId, @Param("recto") String recto,
                                  @Param("verso") String verso, @Param("selfie") String selfie,
                                  @Param("matricule") String matricule);

    /** Rejet d'une demande de rôle professeur sur un compte actif : pièces effacées, la demande peut être refaite. */
    @Transactional
    @Modifying
    @Query(value = "UPDATE ressources.professeurs SET cni_url_front = NULL, cni_url_back = NULL, selfie_url = NULL, "
            + "has_uploaded = false WHERE professeurs_id = :userId", nativeQuery = true)
    int resetProfesseurDocuments(@Param("userId") String userId);

    /**
     * Professeurs à valider : comptes professeur en AWAITING_VALIDATION, plus les comptes ACTIFS
     * (parent, élève…) qui ont demandé le rôle professeur, déposé leurs pièces, et dont le rôle
     * PROFESSOR est encore inactif dans user_roles.
     */
    @Query(value = "SELECT p.professeurs_id FROM ressources.professeurs p "
            + "JOIN ressources.utilisateurs u ON u.id = p.professeurs_id "
            + "WHERE u.etat = 'AWAITING_VALIDATION' "
            // Compte actif (activation partielle, ou parent/élève demandant le rôle) dont les pièces
            // ont été déposées et attendent la décision de l'administrateur.
            + "OR (u.etat = 'ACTIVE' AND p.statut_verification = 'EN_ATTENTE_VALIDATION') "
            + "OR (u.etat = 'ACTIVE' AND p.has_uploaded = true AND p.statut_verification <> 'VALIDE' AND EXISTS ("
            + "  SELECT 1 FROM ressources.user_roles r WHERE r.utilisateur_id = u.id "
            + "  AND r.role_type = 'PROFESSOR' AND r.is_active = false))", nativeQuery = true)
    List<String> findProfesseurIdsEnAttenteDeValidation();

    // ── Statut de vérification du profil professeur (colonnes non mappées en JPA) ─────────────

    @Query(value = "SELECT statut_verification FROM ressources.professeurs WHERE professeurs_id = :userId", nativeQuery = true)
    Optional<String> findStatutVerificationProfesseur(@Param("userId") String userId);

    @Query(value = "SELECT matricule_professeur FROM ressources.professeurs WHERE professeurs_id = :userId", nativeQuery = true)
    Optional<String> findMatriculeProfesseur(@Param("userId") String userId);

    @Query(value = "SELECT motif_rejet_verification FROM ressources.professeurs WHERE professeurs_id = :userId", nativeQuery = true)
    Optional<String> findMotifRejetVerificationProfesseur(@Param("userId") String userId);

    /** Statut (et motif, effacé hors REJETE) du profil professeur ; date du changement = maintenant. */
    @Transactional
    @Modifying
    @Query(value = "UPDATE ressources.professeurs SET statut_verification = :statut, "
            + "motif_rejet_verification = CAST(:motif AS VARCHAR), date_statut_verification = now() "
            + "WHERE professeurs_id = :userId", nativeQuery = true)
    int updateStatutVerificationProfesseur(@Param("userId") String userId, @Param("statut") String statut,
                                           @Param("motif") String motif);

    /**
     * Droits professeur effectifs : profil VALIDE et rôle PROFESSOR non désactivé dans user_roles
     * (comptes antérieurs à user_roles : pas de ligne = actif).
     */
    @Query(value = "SELECT EXISTS (SELECT 1 FROM ressources.professeurs p WHERE p.professeurs_id = :userId "
            + "AND p.statut_verification = 'VALIDE') "
            + "AND NOT EXISTS (SELECT 1 FROM ressources.user_roles r WHERE r.utilisateur_id = :userId "
            + "AND r.role_type = 'PROFESSOR' AND r.is_active = false)", nativeQuery = true)
    boolean isProfesseurValide(@Param("userId") String userId);

}
