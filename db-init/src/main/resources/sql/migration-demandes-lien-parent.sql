SET search_path TO ressources;

-- Demandes de rattachement d'un parent à un élève ayant son propre compte (enfant majeur).
--   EN_ATTENTE        : l'élève doit accepter (ou refuser) le suivi parental
--   EN_ATTENTE_CLASSE : compte élève créé par le parent avec un code de classe ; le lien est activé
--                       automatiquement quand le professeur approuve l'inscription de l'élève
--   ACCEPTEE / REFUSEE
CREATE TABLE IF NOT EXISTS demandes_lien_parent (
    id           VARCHAR(255) NOT NULL PRIMARY KEY,
    parent_id    VARCHAR(255) NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    eleve_id     VARCHAR(255) NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    etat         VARCHAR(30)  NOT NULL DEFAULT 'EN_ATTENTE',
    date_demande TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_reponse TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_demandes_lien_parent_eleve ON demandes_lien_parent(eleve_id);
CREATE INDEX IF NOT EXISTS idx_demandes_lien_parent_parent ON demandes_lien_parent(parent_id);
