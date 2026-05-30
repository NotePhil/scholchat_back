-- ============================================================
-- Production migration — May 2026
-- Safe to run on an existing database (all IF NOT EXISTS / IF NULL guards)
-- Run as: psql -U postgres -d scholchat -f migration-production-2026-05.sql
-- ============================================================

SET search_path TO ressources;

-- 1. creator_id on classes
ALTER TABLE classes ADD COLUMN IF NOT EXISTS creator_id VARCHAR(255);
UPDATE classes SET creator_id = moderator_id WHERE creator_id IS NULL AND moderator_id IS NOT NULL;

-- 2. visibility on evenements
ALTER TABLE evenements ADD COLUMN IF NOT EXISTS visibility VARCHAR(20) DEFAULT 'PUBLIC';
UPDATE evenements SET visibility = 'PUBLIC' WHERE visibility IS NULL;

-- 3. evenement_classes join table
CREATE TABLE IF NOT EXISTS evenement_classes (
    evenement_id VARCHAR(255) NOT NULL,
    classe_id    VARCHAR(255) NOT NULL,
    PRIMARY KEY (evenement_id, classe_id),
    FOREIGN KEY (evenement_id) REFERENCES evenements(id) ON DELETE CASCADE
);

-- 4. notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id                  VARCHAR(255) PRIMARY KEY,
    user_id             VARCHAR(255) NOT NULL,
    type                VARCHAR(100),
    title               VARCHAR(255),
    message             TEXT,
    related_entity_id   VARCHAR(255),
    related_entity_type VARCHAR(100),
    actor_id            VARCHAR(255),
    actor_name          VARCHAR(255),
    is_read             BOOLEAN DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);

-- 5. questions & choices tables
CREATE TABLE IF NOT EXISTS questions_reponses (
    id           VARCHAR(255) PRIMARY KEY,
    intitule     TEXT NOT NULL,
    reponse      TEXT,
    type_question VARCHAR(50),
    points       INTEGER DEFAULT 1,
    exercise_id  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS choix_reponses (
    id              VARCHAR(255) PRIMARY KEY,
    texte           TEXT NOT NULL,
    est_correct     BOOLEAN DEFAULT FALSE,
    ordre_affichage INTEGER DEFAULT 0,
    question_id     VARCHAR(255) NOT NULL,
    CONSTRAINT fk_choix_reponses_question FOREIGN KEY (question_id) REFERENCES questions_reponses(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_choix_reponses_question ON choix_reponses(question_id);

-- 6. Moderator acceder backfill
INSERT INTO acceder (utilisateur_id, classe_id)
SELECT moderator_id, id FROM classes WHERE moderator_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- 7. Re-grant permissions (safe to re-run)
GRANT ALL ON ALL TABLES    IN SCHEMA ressources TO scholchat_user;
GRANT ALL ON ALL SEQUENCES IN SCHEMA ressources TO scholchat_user;
