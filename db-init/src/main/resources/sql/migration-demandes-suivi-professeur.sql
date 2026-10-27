SET search_path TO ressources;

-- Suivi parental d'un élève majeur PAR CLASSE (changeset 31) : une demande par (parent, élève, classe), décidée par
-- un gestionnaire de CETTE classe (ou un administrateur), plus par l'élève. La demande ACCEPTEE vaut autorisation de
-- lecture du parent pour cette classe ; le lien parent_eleve existe tant qu'au moins une classe est ACCEPTEE.
-- Nouveaux états : ANNULEE (annulée par le parent) et RETIREE (suivi retiré par un gestionnaire / un admin).
-- Idempotent.
ALTER TABLE demandes_lien_parent ADD COLUMN IF NOT EXISTS motif VARCHAR(500);
ALTER TABLE demandes_lien_parent ADD COLUMN IF NOT EXISTS traite_par VARCHAR(255);
ALTER TABLE demandes_lien_parent ADD COLUMN IF NOT EXISTS classe_id VARCHAR(255);
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_demandes_lien_parent_classe') THEN
        ALTER TABLE demandes_lien_parent ADD CONSTRAINT fk_demandes_lien_parent_classe
            FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE;
    END IF;
END
$$;

-- Ancien état EN_ATTENTE_CLASSE (compte élève créé par le parent, supprimé) : redevient EN_ATTENTE si le compte de
-- l'élève est actif (éclatée par classe ci-dessous), sinon ANNULEE avec une note.
UPDATE demandes_lien_parent d SET etat = 'EN_ATTENTE'
WHERE d.etat = 'EN_ATTENTE_CLASSE'
  AND EXISTS (SELECT 1 FROM utilisateurs u WHERE u.id = d.eleve_id AND u.etat = 'ACTIVE');
UPDATE demandes_lien_parent SET etat = 'ANNULEE', date_reponse = CURRENT_TIMESTAMP,
       motif = 'Annulée : le compte élève de l''enfant n''est pas actif. Il doit d''abord créer son compte élève.'
WHERE etat = 'EN_ATTENTE_CLASSE';

-- Demandes sans classe (avant ce changeset) : une demande par classe dont l'élève est membre, même état.
INSERT INTO demandes_lien_parent (id, parent_id, eleve_id, classe_id, etat, date_demande, date_reponse)
SELECT md5(d.id || a.classe_id), d.parent_id, d.eleve_id, a.classe_id, d.etat, d.date_demande, d.date_reponse
FROM demandes_lien_parent d JOIN acceder a ON a.utilisateur_id = d.eleve_id
WHERE d.classe_id IS NULL AND d.etat IN ('EN_ATTENTE', 'ACCEPTEE')
ON CONFLICT (id) DO NOTHING;
UPDATE demandes_lien_parent SET etat = 'ANNULEE', date_reponse = COALESCE(date_reponse, CURRENT_TIMESTAMP),
       motif = 'Remplacée par une demande par classe'
WHERE classe_id IS NULL AND etat IN ('EN_ATTENTE', 'ACCEPTEE');

-- Liens existants vers un élève ayant son propre compte (e-mail ou mot de passe), sans suivi accepté : suivi
-- ACCEPTEE pour chacune de ses classes (le parent garde ce qu'il voyait).
INSERT INTO demandes_lien_parent (id, parent_id, eleve_id, classe_id, etat, date_demande, date_reponse, motif)
SELECT md5('lien-existant' || pe.parent_id || pe.eleve_id || a.classe_id), pe.parent_id, pe.eleve_id, a.classe_id,
       'ACCEPTEE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Lien existant avant le suivi par classe'
FROM parent_eleve pe
JOIN utilisateurs u ON u.id = pe.eleve_id
JOIN acceder a ON a.utilisateur_id = pe.eleve_id
WHERE (NULLIF(TRIM(u.email), '') IS NOT NULL OR NULLIF(TRIM(u.passeaccess), '') IS NOT NULL)
  AND NOT EXISTS (SELECT 1 FROM demandes_lien_parent d WHERE d.parent_id = pe.parent_id AND d.eleve_id = pe.eleve_id
                  AND d.classe_id = a.classe_id AND d.etat IN ('EN_ATTENTE', 'ACCEPTEE'))
ON CONFLICT (id) DO NOTHING;

-- Une seule demande ouverte / acceptée par (parent, élève, classe).
CREATE UNIQUE INDEX IF NOT EXISTS uq_demandes_lien_parent_ouverte
    ON demandes_lien_parent (parent_id, eleve_id, classe_id) WHERE etat IN ('EN_ATTENTE', 'ACCEPTEE');
CREATE INDEX IF NOT EXISTS idx_demandes_lien_parent_classe ON demandes_lien_parent (classe_id);
