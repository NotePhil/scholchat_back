-- Migration: live session tables

CREATE TABLE IF NOT EXISTS ressources.cours_sessions (
    id                  VARCHAR(255) PRIMARY KEY,
    cours_id            VARCHAR(255) NOT NULL,
    room_name           VARCHAR(512) NOT NULL UNIQUE,
    mode                VARCHAR(50)  NOT NULL,
    status              VARCHAR(50)  NOT NULL,
    started_at          TIMESTAMP,
    ended_at            TIMESTAMP,
    started_by_user_id  VARCHAR(255) NOT NULL,
    current_chapitre_id VARCHAR(255),
    FOREIGN KEY (cours_id) REFERENCES ressources.cours(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cours_sessions_cours_status
    ON ressources.cours_sessions(cours_id, status);

CREATE TABLE IF NOT EXISTS ressources.cours_session_participants (
    session_id VARCHAR(255) NOT NULL,
    user_id    VARCHAR(255) NOT NULL,
    PRIMARY KEY (session_id, user_id),
    FOREIGN KEY (session_id) REFERENCES ressources.cours_sessions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS ressources.chapitre_progress (
    id           VARCHAR(255) PRIMARY KEY,
    user_id      VARCHAR(255) NOT NULL,
    chapitre_id  VARCHAR(255) NOT NULL,
    cours_id     VARCHAR(255) NOT NULL,
    completed    BOOLEAN      NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    UNIQUE (user_id, chapitre_id),
    FOREIGN KEY (user_id)     REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (cours_id)    REFERENCES ressources.cours(id)        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_chapitre_progress_user_cours
    ON ressources.chapitre_progress(user_id, cours_id);
