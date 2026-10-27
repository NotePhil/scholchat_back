SET search_path TO ressources;

-- Temps de lecture d'un chapitre (secondes) mesuré par le client entre l'ouverture du chapitre (ou la
-- complétion précédente) et le clic « Marquer comme terminé » ; cumulé si le chapitre est re-terminé.
ALTER TABLE progression_chapitre ADD COLUMN IF NOT EXISTS duree_secondes BIGINT NOT NULL DEFAULT 0;
