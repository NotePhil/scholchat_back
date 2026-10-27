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
        // Date de publication d'un événement (changeset 27-evenements-date-publication)
        "ALTER TABLE ressources.evenements ADD COLUMN IF NOT EXISTS date_publication TIMESTAMP",
        // Temps de lecture d'un chapitre (changeset 28-progression-chapitre-duree)
        "ALTER TABLE ressources.progression_chapitre ADD COLUMN IF NOT EXISTS duree_secondes BIGINT NOT NULL DEFAULT 0",
        // Messagerie de type chat (changeset 30-conversations) ; contraintes et rattrapage plus bas (PostgreSQL)
        "CREATE TABLE IF NOT EXISTS ressources.conversations (id VARCHAR(36) NOT NULL PRIMARY KEY, type VARCHAR(10) NOT NULL, "
                + "titre VARCHAR(255), classe_id VARCHAR(255), created_by VARCHAR(255), "
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, last_message_at TIMESTAMP, "
                + "direct_key VARCHAR(520) UNIQUE, participants_key VARCHAR(32))",
        "CREATE INDEX IF NOT EXISTS idx_conversations_participants_key ON ressources.conversations (participants_key)",
        "CREATE TABLE IF NOT EXISTS ressources.conversation_participants ("
                + "conversation_id VARCHAR(36) NOT NULL REFERENCES ressources.conversations (id) ON DELETE CASCADE, "
                + "utilisateur_id VARCHAR(255) NOT NULL REFERENCES ressources.utilisateurs (id) ON DELETE CASCADE, "
                + "joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, last_read_at TIMESTAMP, "
                + "muted BOOLEAN NOT NULL DEFAULT FALSE, deleted_at TIMESTAMP, PRIMARY KEY (conversation_id, utilisateur_id))",
        "CREATE INDEX IF NOT EXISTS idx_conversation_participants_user ON ressources.conversation_participants (utilisateur_id)",
        "ALTER TABLE ressources.messages ADD COLUMN IF NOT EXISTS conversation_id VARCHAR(36)",
        "ALTER TABLE ressources.messages ADD COLUMN IF NOT EXISTS reply_to_id VARCHAR(255)",
        "ALTER TABLE ressources.messages ADD COLUMN IF NOT EXISTS date_envoi TIMESTAMP",
        "CREATE INDEX IF NOT EXISTS idx_messages_conversation_date ON ressources.messages (conversation_id, date_envoi)",
        // Demandes de suivi parental d'un élève ayant son propre compte (changeset 29-demandes-lien-parent)
        "CREATE TABLE IF NOT EXISTS ressources.demandes_lien_parent (id VARCHAR(255) NOT NULL PRIMARY KEY, "
                + "parent_id VARCHAR(255) NOT NULL REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE, "
                + "eleve_id VARCHAR(255) NOT NULL REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE, "
                + "etat VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE', date_demande TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "date_reponse TIMESTAMP)",
        "CREATE INDEX IF NOT EXISTS idx_demandes_lien_parent_eleve ON ressources.demandes_lien_parent(eleve_id)",
        "CREATE INDEX IF NOT EXISTS idx_demandes_lien_parent_parent ON ressources.demandes_lien_parent(parent_id)",
        // Demandes de suivi validées par le professeur de la classe (changeset 31-demandes-suivi-professeur)
        "ALTER TABLE ressources.demandes_lien_parent ADD COLUMN IF NOT EXISTS motif VARCHAR(500)",
        "ALTER TABLE ressources.demandes_lien_parent ADD COLUMN IF NOT EXISTS traite_par VARCHAR(255)",
        // Suivi parental par classe (changeset 31) ; reprise des anciennes lignes : migration-demandes-suivi-professeur.sql
        "ALTER TABLE ressources.demandes_lien_parent ADD COLUMN IF NOT EXISTS classe_id VARCHAR(255)",
        "CREATE INDEX IF NOT EXISTS idx_demandes_lien_parent_classe ON ressources.demandes_lien_parent (classe_id)",
        // Messagerie : favori, « marquer comme non lu » et suppression définitive depuis la corbeille, par participant
        // (changeset 32-conversations-favoris-corbeille)
        "ALTER TABLE ressources.conversation_participants ADD COLUMN IF NOT EXISTS favori BOOLEAN NOT NULL DEFAULT FALSE",
        "ALTER TABLE ressources.conversation_participants ADD COLUMN IF NOT EXISTS marque_non_lu BOOLEAN NOT NULL DEFAULT FALSE",
        "ALTER TABLE ressources.conversation_participants ADD COLUMN IF NOT EXISTS historique_depuis TIMESTAMP",
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

    /** Contraintes des conversations (changeset 30-conversations), même contenu que migration-conversations.sql. */
    private static final String CONVERSATIONS_CONTRAINTES_SQL = """
        DO $$
        BEGIN
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                           WHERE constraint_schema = 'ressources' AND table_name = 'messages'
                             AND constraint_name = 'fk_messages_conversation') THEN
                ALTER TABLE ressources.messages ADD CONSTRAINT fk_messages_conversation
                    FOREIGN KEY (conversation_id) REFERENCES ressources.conversations (id) ON DELETE SET NULL;
            END IF;
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                           WHERE constraint_schema = 'ressources' AND table_name = 'messages'
                             AND constraint_name = 'fk_messages_reply_to') THEN
                ALTER TABLE ressources.messages ADD CONSTRAINT fk_messages_reply_to
                    FOREIGN KEY (reply_to_id) REFERENCES ressources.messages (id) ON DELETE SET NULL;
            END IF;
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                           WHERE constraint_schema = 'ressources' AND table_name = 'conversations'
                             AND constraint_name = 'fk_conversations_classe') THEN
                ALTER TABLE ressources.conversations ADD CONSTRAINT fk_conversations_classe
                    FOREIGN KEY (classe_id) REFERENCES ressources.classes (id) ON DELETE SET NULL;
            END IF;
            IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                           WHERE constraint_schema = 'ressources' AND table_name = 'conversations'
                             AND constraint_name = 'fk_conversations_created_by') THEN
                ALTER TABLE ressources.conversations ADD CONSTRAINT fk_conversations_created_by
                    FOREIGN KEY (created_by) REFERENCES ressources.utilisateurs (id) ON DELETE SET NULL;
            END IF;
        END
        $$;
        """;

    /**
     * Rattachement des messages sans conversation (changeset 30-conversations), même contenu que
     * migration-conversations.sql : ne touche que les messages dont conversation_id est NULL.
     */
    private static final String CONVERSATIONS_RATTRAPAGE_SQL = """
        DO $$
        DECLARE
            r RECORD;
            d TIMESTAMP;
        BEGIN
            PERFORM set_config('TimeZone', 'UTC', true);

            -- 1. date_envoi depuis datecreation (ISO, "yyyy-MM-dd HH:mm:ss" ou ancien Date#toString()).
            FOR r IN SELECT id, datecreation FROM ressources.messages WHERE date_envoi IS NULL LOOP
                d := NULL;
                BEGIN
                    d := (r.datecreation::timestamptz AT TIME ZONE 'UTC');
                EXCEPTION WHEN others THEN
                    BEGIN
                        d := regexp_replace(r.datecreation,
                                '^[A-Za-z]{3} ([A-Za-z]{3} [0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}) \\S+ ([0-9]{4})$', '\\1 \\2')::timestamp;
                    EXCEPTION WHEN others THEN
                        d := NULL;
                    END;
                END;
                UPDATE ressources.messages SET date_envoi = COALESCE(d, TIMESTAMP '2000-01-01') WHERE id = r.id;
            END LOOP;

            -- 2. Ensemble des participants (expéditeur + destinataires) de chaque message sans conversation.
            DROP TABLE IF EXISTS tmp_conv_msg;
            CREATE TEMP TABLE tmp_conv_msg AS
            SELECT x.message_id, x.expediteur_id, x.objet, x.date_envoi, x.pkey,
                   array_length(string_to_array(x.pkey, '|'), 1) AS n,
                   (SELECT CASE WHEN count(*) = 1 THEN min(mc.classe_id) END
                      FROM ressources.message_classes mc WHERE mc.message_id = x.message_id) AS classe_id
            FROM (
                SELECT m.id AS message_id, m.expediteur_id, m.objet, m.date_envoi,
                       (SELECT string_agg(p.u, '|' ORDER BY p.u COLLATE "C")
                          FROM (SELECT m.expediteur_id AS u WHERE m.expediteur_id IS NOT NULL
                                UNION
                                SELECT rc.utilisateur_id FROM ressources.recevoir rc
                                 WHERE rc.message_id = m.id AND rc.utilisateur_id IS NOT NULL) p) AS pkey
                FROM ressources.messages m
                WHERE m.conversation_id IS NULL
            ) x
            WHERE x.pkey IS NOT NULL;

            -- 2a. Deux personnes (ou message à soi-même) : conversation DIRECT unique par paire.
            INSERT INTO ressources.conversations (id, type, created_by, created_at, direct_key, participants_key)
            SELECT DISTINCT ON (dkey) md5('D|' || dkey)::uuid::text, 'DIRECT', expediteur_id, date_envoi, dkey, md5(dkey)
            FROM (SELECT t.*, CASE WHEN t.n = 1 THEN t.pkey || '|' || t.pkey ELSE t.pkey END AS dkey
                    FROM tmp_conv_msg t WHERE t.n <= 2) s
            ORDER BY dkey, date_envoi
            ON CONFLICT DO NOTHING;

            UPDATE ressources.messages m SET conversation_id = c.id
            FROM tmp_conv_msg t
            JOIN ressources.conversations c
              ON c.direct_key = CASE WHEN t.n = 1 THEN t.pkey || '|' || t.pkey ELSE t.pkey END
            WHERE t.n <= 2 AND m.id = t.message_id;

            -- 2b. Plus de deux personnes : conversation GROUPE par ensemble de participants, titrée par l'objet.
            INSERT INTO ressources.conversations (id, type, titre, classe_id, created_by, created_at, participants_key)
            SELECT DISTINCT ON (md5(t.pkey)) md5('G|' || t.pkey)::uuid::text, 'GROUPE', left(t.objet, 255), t.classe_id,
                   t.expediteur_id, t.date_envoi, md5(t.pkey)
            FROM tmp_conv_msg t
            WHERE t.n > 2
              AND NOT EXISTS (SELECT 1 FROM ressources.conversations c
                               WHERE c.type = 'GROUPE' AND c.participants_key = md5(t.pkey))
            ORDER BY md5(t.pkey), t.date_envoi
            ON CONFLICT DO NOTHING;

            UPDATE ressources.messages m SET conversation_id =
                   (SELECT c.id FROM ressources.conversations c
                     WHERE c.type = 'GROUPE' AND c.participants_key = md5(t.pkey)
                     ORDER BY c.last_message_at DESC NULLS LAST, c.created_at DESC LIMIT 1)
            FROM tmp_conv_msg t
            WHERE t.n > 2 AND m.id = t.message_id;

            -- 3. Participants.
            INSERT INTO ressources.conversation_participants (conversation_id, utilisateur_id, joined_at, muted)
            SELECT DISTINCT m.conversation_id, u.uid, c.created_at, FALSE
            FROM tmp_conv_msg t
            JOIN ressources.messages m ON m.id = t.message_id
            JOIN ressources.conversations c ON c.id = m.conversation_id
            CROSS JOIN LATERAL unnest(string_to_array(t.pkey, '|')) AS u(uid)
            WHERE EXISTS (SELECT 1 FROM ressources.utilisateurs x WHERE x.id = u.uid)
            ON CONFLICT DO NOTHING;

            -- 4. Date du dernier message.
            UPDATE ressources.conversations c SET last_message_at = s.mx
            FROM (SELECT conversation_id, max(date_envoi) AS mx FROM ressources.messages
                   WHERE conversation_id IS NOT NULL GROUP BY conversation_id) s
            WHERE c.id = s.conversation_id AND (c.last_message_at IS NULL OR c.last_message_at < s.mx);

            -- 5. Dernière lecture : juste avant le plus ancien message reçu non lu (statut lu conservé),
            --    sinon le dernier message de la conversation.
            UPDATE ressources.conversation_participants cp SET last_read_at = COALESCE(
                   (SELECT min(m.date_envoi) - INTERVAL '1 millisecond'
                      FROM ressources.messages m
                      LEFT JOIN ressources.message_statut s ON s.message_id = m.id AND s.utilisateur_id = cp.utilisateur_id
                     WHERE m.conversation_id = cp.conversation_id
                       AND m.expediteur_id IS DISTINCT FROM cp.utilisateur_id
                       AND m.deleted = FALSE
                       AND COALESCE(s.lu, FALSE) = FALSE
                       AND COALESCE(s.supprime, FALSE) = FALSE),
                   (SELECT c.last_message_at FROM ressources.conversations c WHERE c.id = cp.conversation_id))
            WHERE cp.last_read_at IS NULL
              AND cp.conversation_id IN (SELECT DISTINCT m.conversation_id FROM tmp_conv_msg t
                                           JOIN ressources.messages m ON m.id = t.message_id);

            DROP TABLE IF EXISTS tmp_conv_msg;
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

            // Conversations : contraintes + rattrapage des anciens messages (PostgreSQL uniquement).
            try {
                if ("PostgreSQL".equalsIgnoreCase(conn.getMetaData().getDatabaseProductName())) {
                    stmt.execute(CONVERSATIONS_CONTRAINTES_SQL);
                    stmt.execute(CONVERSATIONS_RATTRAPAGE_SQL);
                }
            } catch (Exception e) {
                log.warn("Startup schema patch (conversations) failed: {}", e.getMessage());
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
