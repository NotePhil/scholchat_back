SET search_path TO ressources;

-- Message bodies were capped at 255 chars (varchar(255)).
ALTER TABLE messages ALTER COLUMN contenu TYPE TEXT;

-- Per-user deletion: "supprimer pour moi" hides the message for one user only
-- (moved to that user's trash); "purge" = permanently removed from that user's trash.
ALTER TABLE message_statut ADD COLUMN IF NOT EXISTS supprime BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE message_statut ADD COLUMN IF NOT EXISTS purge BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE message_statut ADD COLUMN IF NOT EXISTS date_suppression TIMESTAMP;
CREATE INDEX IF NOT EXISTS idx_message_statut_supprime ON message_statut (utilisateur_id) WHERE supprime = TRUE;

-- Message attachments (images, videos, documents). Files live in object storage
-- (uploaded through POST /media/presigned-url); this table references their keys.
CREATE TABLE IF NOT EXISTS message_medias (
    id             VARCHAR(36)  PRIMARY KEY,
    message_id     VARCHAR(255) NOT NULL,
    file_name      VARCHAR(255) NOT NULL,
    file_path      VARCHAR(512) NOT NULL,
    content_type   VARCHAR(150),
    media_type     VARCHAR(20),
    file_size      BIGINT,
    ordre          INTEGER      NOT NULL DEFAULT 0,
    date_creation  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_message_medias_message FOREIGN KEY (message_id) REFERENCES messages (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_message_medias_message ON message_medias (message_id);
