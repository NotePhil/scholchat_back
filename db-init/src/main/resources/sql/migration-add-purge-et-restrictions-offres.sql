SET search_path TO ressources;

-- Delais de purge automatique (en minutes), configurables par l'admin a tout moment depuis
-- l'ecran de gestion des offres. Nul = pas de rappel/suppression automatique pour cette offre.
ALTER TABLE offres ADD COLUMN IF NOT EXISTS delai_rappel_suppression_minutes BIGINT;
ALTER TABLE offres ADD COLUMN IF NOT EXISTS delai_suppression_minutes BIGINT;

-- Restrictions informatives (non appliquees pour le moment, affichees a titre indicatif)
ALTER TABLE offres ADD COLUMN IF NOT EXISTS eleves_max INTEGER;
ALTER TABLE offres ADD COLUMN IF NOT EXISTS stockage_max_go INTEGER;
ALTER TABLE offres ADD COLUMN IF NOT EXISTS messagerie_incluse BOOLEAN;

-- Suivi du rappel de suppression deja envoye (evite les doublons) + tracabilite des offres
-- attribuees manuellement par un admin (support), sans passer par le paiement simule.
ALTER TABLE contrats ADD COLUMN IF NOT EXISTS date_rappel_suppression_envoye TIMESTAMP;
ALTER TABLE contrats ADD COLUMN IF NOT EXISTS accorde_par_admin BOOLEAN NOT NULL DEFAULT FALSE;

-- Un etablissement n'avait pas de statut propre : necessaire pour afficher un badge "Offre
-- expiree" dans sa liste, au meme titre que les classes.
ALTER TABLE etablissements ADD COLUMN IF NOT EXISTS expire_par_offre BOOLEAN NOT NULL DEFAULT FALSE;

-- Valeurs par defaut sur les offres de demarrage deja seedees : rappel 2 jours apres expiration,
-- suppression definitive 3 jours apres expiration si toujours pas renouvelee.
UPDATE offres
SET delai_rappel_suppression_minutes = 2880, delai_suppression_minutes = 4320
WHERE id IN ('00000000-offre-classe-standard', '00000000-offre-etablissement-bronze')
  AND delai_suppression_minutes IS NULL;

-- Offre de test : rappel a 1 minute apres expiration, suppression definitive a 2 minutes -
-- permet de valider tout le pipeline (expiration -> rappel -> suppression) en quelques minutes.
UPDATE offres
SET delai_rappel_suppression_minutes = 1, delai_suppression_minutes = 2
WHERE id = '00000000-offre-test-expiration'
  AND delai_suppression_minutes IS NULL;
