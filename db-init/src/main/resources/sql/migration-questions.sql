-- Migration pour supporter tous les types de questions

SET search_path TO ressources;

-- Ajouter les nouvelles colonnes à questions_reponses
ALTER TABLE questions_reponses 
ADD COLUMN IF NOT EXISTS reponse_attendue_vrai_faux BOOLEAN,
ADD COLUMN IF NOT EXISTS reponse_attendue_courte TEXT,
ADD COLUMN IF NOT EXISTS reponse_attendue_longue TEXT,
ADD COLUMN IF NOT EXISTS points DOUBLE PRECISION;

-- Créer la table choix_reponses pour QCM, ASSOCIATION, CLASSEMENT
CREATE TABLE IF NOT EXISTS choix_reponses (
    id VARCHAR(255) PRIMARY KEY,
    texte VARCHAR(1000) NOT NULL,
    est_correct BOOLEAN NOT NULL DEFAULT FALSE,
    ordre_affichage INTEGER,
    question_id VARCHAR(255) NOT NULL,
    CONSTRAINT fk_choix_reponses_question 
        FOREIGN KEY (question_id) 
        REFERENCES questions_reponses(id) 
        ON DELETE CASCADE
);

-- Créer les index
CREATE INDEX IF NOT EXISTS idx_choix_reponses_question ON choix_reponses(question_id);
CREATE INDEX IF NOT EXISTS idx_choix_reponses_ordre ON choix_reponses(ordre_affichage);
