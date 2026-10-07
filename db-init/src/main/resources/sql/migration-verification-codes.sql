-- Vérification du compte par code envoyé par e-mail (bouton « Vérifier mon compte » de la page de connexion) :
-- code à 6 chiffres valable 10 minutes, stocké haché (SHA-256), 5 essais au plus, renvoi après 60 s.
-- Une seule ligne active par utilisateur (remplacée à chaque envoi, supprimée après vérification réussie).
SET search_path TO ressources;

CREATE TABLE IF NOT EXISTS ressources.verification_codes (
    id             VARCHAR(255) NOT NULL PRIMARY KEY,
    utilisateur_id VARCHAR(255) NOT NULL REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE,
    code_hash      VARCHAR(128) NOT NULL,
    expires_at     TIMESTAMP    NOT NULL,
    attempts       INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_verification_codes_utilisateur ON ressources.verification_codes(utilisateur_id);
