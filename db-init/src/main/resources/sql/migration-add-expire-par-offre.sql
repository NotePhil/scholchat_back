SET search_path TO ressources;

ALTER TABLE classes ADD COLUMN IF NOT EXISTS expire_par_offre BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX IF NOT EXISTS idx_classes_expire_par_offre ON classes(expire_par_offre) WHERE expire_par_offre = TRUE;
