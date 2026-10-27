-- Messagerie de type « chat » (WhatsApp) : conversations, participants, réponses.
-- Idempotent : peut être rejoué sans effet (IF NOT EXISTS, contraintes vérifiées, rattrapage
-- limité aux messages sans conversation). Même contenu que StartupSchemaPatcher (CONVERSATIONS_*).

CREATE TABLE IF NOT EXISTS ressources.conversations (
    id               VARCHAR(36)  NOT NULL PRIMARY KEY,
    type             VARCHAR(10)  NOT NULL,              -- DIRECT | GROUPE
    titre            VARCHAR(255),
    classe_id        VARCHAR(255),
    created_by       VARCHAR(255),
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at  TIMESTAMP,
    -- DIRECT : "<idMin>|<idMax>" (ordre binaire) => une seule conversation par paire
    direct_key       VARCHAR(520) UNIQUE,
    -- md5 des identifiants triés des participants joints par '|' (rattachement des anciens envois)
    participants_key VARCHAR(32)
);
CREATE INDEX IF NOT EXISTS idx_conversations_participants_key ON ressources.conversations (participants_key);

CREATE TABLE IF NOT EXISTS ressources.conversation_participants (
    conversation_id VARCHAR(36)  NOT NULL REFERENCES ressources.conversations (id) ON DELETE CASCADE,
    utilisateur_id  VARCHAR(255) NOT NULL REFERENCES ressources.utilisateurs (id) ON DELETE CASCADE,
    joined_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_read_at    TIMESTAMP,
    muted           BOOLEAN      NOT NULL DEFAULT FALSE,
    -- « supprimer la conversation pour moi » : masquée jusqu'au prochain message, historique antérieur caché
    deleted_at      TIMESTAMP,
    PRIMARY KEY (conversation_id, utilisateur_id)
);
CREATE INDEX IF NOT EXISTS idx_conversation_participants_user ON ressources.conversation_participants (utilisateur_id);

ALTER TABLE ressources.messages ADD COLUMN IF NOT EXISTS conversation_id VARCHAR(36);
ALTER TABLE ressources.messages ADD COLUMN IF NOT EXISTS reply_to_id VARCHAR(255);
-- Date d'envoi typée (datecreation est une chaîne aux formats hétérogènes) : tri et pagination.
ALTER TABLE ressources.messages ADD COLUMN IF NOT EXISTS date_envoi TIMESTAMP;
CREATE INDEX IF NOT EXISTS idx_messages_conversation_date ON ressources.messages (conversation_id, date_envoi);

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

-- Rattrapage des messages existants.
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
                        '^[A-Za-z]{3} ([A-Za-z]{3} [0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}) \S+ ([0-9]{4})$', '\1 \2')::timestamp;
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
