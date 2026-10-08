package cmr.notep.business.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Self-healing schema patch for gaps not (yet) captured by an automatic
 * migration runner. This exists because the dev database container used
 * during development runs on an ephemeral volume that gets wiped on every
 * restart, silently reverting any migration applied by hand — the
 * media.question_id column (added for question attachments) kept
 * disappearing and breaking uploads. Re-applying it here, on every
 * application startup, makes it self-healing regardless of how or when the
 * database gets (re)provisioned.
 *
 * Every statement is intentionally idempotent (IF NOT EXISTS, or an
 * existence check before adding a constraint) and executed independently,
 * so a statement that's already applied — or fails for any reason — never
 * blocks another fix or application startup.
 */
@Component
@Slf4j
public class StartupSchemaPatcher implements ApplicationRunner {

    private static final String[] IDEMPOTENT_STATEMENTS = {
        "ALTER TABLE ressources.media ADD COLUMN IF NOT EXISTS question_id VARCHAR(255)",
        "CREATE INDEX IF NOT EXISTS idx_media_question_id ON ressources.media(question_id)",
        // UtilisateursEntity mappe reset_password_token (jeton du lien de réinitialisation, usage unique) :
        // sans cette colonne toute lecture d'utilisateur échouerait.
        "ALTER TABLE ressources.utilisateurs ADD COLUMN IF NOT EXISTS reset_password_token TEXT",
        // Mot de passe temporaire à changer à la première connexion (changeset 23-utilisateurs-must-change-password)
        "ALTER TABLE ressources.utilisateurs ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE",
        // Codes de vérification du compte par e-mail (changeset 24-verification-codes)
        "CREATE TABLE IF NOT EXISTS ressources.verification_codes (id VARCHAR(255) NOT NULL PRIMARY KEY, "
                + "utilisateur_id VARCHAR(255) NOT NULL REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE, "
                + "code_hash VARCHAR(128) NOT NULL, expires_at TIMESTAMP NOT NULL, attempts INTEGER NOT NULL DEFAULT 0, "
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
        "CREATE INDEX IF NOT EXISTS idx_verification_codes_utilisateur ON ressources.verification_codes(utilisateur_id)",
        // Exercice programmé rattaché à un cours (changeset 25-exercise-programmer-cours) : NULL seulement sur d'anciennes lignes (ignorées).
        // La clé étrangère (ON DELETE SET NULL) est ajoutée plus bas après vérification.
        "ALTER TABLE ressources.exercises_programmer ADD COLUMN IF NOT EXISTS cours_id VARCHAR(255)",
        "CREATE INDEX IF NOT EXISTS idx_exercises_programmer_cours ON ressources.exercises_programmer(cours_id)",
        "CREATE INDEX IF NOT EXISTS idx_cours_programmer_classes_classe ON ressources.cours_programmer_classes(classe_id)",
        "CREATE INDEX IF NOT EXISTS idx_questions_reponses_exercise ON ressources.questions_reponses(exercise_id)",
    };

    /**
     * Statut de vérification du profil professeur (changeset 22-professeur-statut-verification) :
     * le bloc n'ajoute la colonne ET ne fait le rattrapage que si la colonne n'existe pas encore,
     * donc il ne réécrit jamais des décisions de l'administrateur. Même contenu que
     * db-init/src/main/resources/sql/migration-professeur-statut-verification.sql.
     */
    private static final String PROFESSEUR_STATUT_VERIFICATION_SQL = """
        DO $$
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                           WHERE table_schema = 'ressources' AND table_name = 'professeurs'
                             AND column_name = 'statut_verification') THEN

                ALTER TABLE ressources.professeurs
                    ADD COLUMN statut_verification VARCHAR(30) NOT NULL DEFAULT 'DOCUMENTS_MANQUANTS';

                -- Rattrapage. "Pièces complètes" = les 3 pièces renseignées.
                UPDATE ressources.professeurs p SET statut_verification = CASE
                    WHEN u.etat = 'REJECTED' THEN 'REJETE'
                    WHEN NULLIF(TRIM(p.cni_url_front), '') IS NULL
                         OR NULLIF(TRIM(p.cni_url_back), '') IS NULL
                         OR NULLIF(TRIM(p.selfie_url), '') IS NULL
                        THEN 'DOCUMENTS_MANQUANTS'
                    -- Pièces complètes, compte en attente de validation : l'administrateur doit encore décider.
                    WHEN u.etat = 'AWAITING_VALIDATION' THEN 'EN_ATTENTE_VALIDATION'
                    -- Compte actif (parent, élève…) dont la demande de rôle professeur n'est pas encore validée.
                    WHEN EXISTS (SELECT 1 FROM ressources.user_roles r
                                 WHERE r.utilisateur_id = u.id AND r.role_type = 'PROFESSOR' AND r.is_active = false)
                        THEN 'EN_ATTENTE_VALIDATION'
                    -- ACTIVE (ou validé en attente de mot de passe, suspendu…) avec pièces complètes : déjà validé.
                    ELSE 'VALIDE'
                END
                FROM ressources.utilisateurs u
                WHERE u.id = p.professeurs_id;

                -- has_uploaded recalculé à partir des pièces réellement présentes.
                UPDATE ressources.professeurs SET has_uploaded =
                    (NULLIF(TRIM(cni_url_front), '') IS NOT NULL AND NULLIF(TRIM(cni_url_back), '') IS NOT NULL
                     AND NULLIF(TRIM(selfie_url), '') IS NOT NULL);
            END IF;

            ALTER TABLE ressources.professeurs ADD COLUMN IF NOT EXISTS motif_rejet_verification TEXT;
            ALTER TABLE ressources.professeurs ADD COLUMN IF NOT EXISTS date_statut_verification TIMESTAMPTZ;
            CREATE INDEX IF NOT EXISTS idx_professeurs_statut_verification ON ressources.professeurs(statut_verification);
        END
        $$;
        """;

    private final DataSource dataSource;

    public StartupSchemaPatcher(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : IDEMPOTENT_STATEMENTS) {
                try {
                    stmt.execute(sql);
                } catch (Exception e) {
                    log.warn("Startup schema patch statement failed (likely fine, already applied): {} — {}", sql, e.getMessage());
                }
            }

            // Statut de vérification du professeur (PostgreSQL uniquement : bloc DO plpgsql ;
            // le schéma H2 déclare déjà les colonnes).
            try {
                if ("PostgreSQL".equalsIgnoreCase(conn.getMetaData().getDatabaseProductName())) {
                    stmt.execute(PROFESSEUR_STATUT_VERIFICATION_SQL);
                }
            } catch (Exception e) {
                log.warn("Startup schema patch (professeurs.statut_verification) failed: {}", e.getMessage());
            }

            // FK constraints aren't naturally idempotent in Postgres (no
            // "ADD CONSTRAINT IF NOT EXISTS") — check first.
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM information_schema.table_constraints " +
                    "WHERE constraint_schema = 'ressources' AND table_name = 'media' " +
                    "AND constraint_name = 'fk_media_question'")) {
                if (!rs.next()) {
                    stmt.execute(
                        "ALTER TABLE ressources.media ADD CONSTRAINT fk_media_question " +
                        "FOREIGN KEY (question_id) REFERENCES ressources.questions_reponses(id) ON DELETE CASCADE");
                    log.info("Startup schema patch: added fk_media_question constraint");
                }
            } catch (Exception e) {
                log.warn("Could not verify/add fk_media_question constraint: {}", e.getMessage());
            }

            // exercises_programmer.cours_id -> cours(id) ON DELETE SET NULL (changeset 25)
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM information_schema.table_constraints " +
                    "WHERE constraint_schema = 'ressources' AND table_name = 'exercises_programmer' " +
                    "AND constraint_name = 'fk_exercises_programmer_cours'")) {
                if (!rs.next()) {
                    stmt.execute(
                        "ALTER TABLE ressources.exercises_programmer ADD CONSTRAINT fk_exercises_programmer_cours " +
                        "FOREIGN KEY (cours_id) REFERENCES ressources.cours(id) ON DELETE SET NULL");
                    log.info("Startup schema patch: added fk_exercises_programmer_cours constraint");
                }
            } catch (Exception e) {
                log.warn("Could not verify/add fk_exercises_programmer_cours constraint: {}", e.getMessage());
            }

            log.info("Startup schema patch complete (media.question_id column/index/constraint).");
        } catch (Exception e) {
            log.error("Startup schema patch failed entirely — question media uploads may not work: {}", e.getMessage());
        }
    }
}
