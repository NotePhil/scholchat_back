-- Migration: type_assignation + etat_soumission
-- Run this on existing databases after deploying the backend changes.

SET search_path TO ressources;

-- 1. Add type_assignation to exercises_programmer (EXERCICE | DEVOIR)
ALTER TABLE exercises_programmer
    ADD COLUMN IF NOT EXISTS type_assignation VARCHAR(50) NOT NULL DEFAULT 'EXERCICE';

-- 2. Add etat_soumission to participer_exo (EN_COURS | SOUMIS | EN_ATTENTE_CORRECTION | CORRIGE)
ALTER TABLE participer_exo
    ADD COLUMN IF NOT EXISTS etat_soumission VARCHAR(50) NOT NULL DEFAULT 'EN_COURS';

-- 3. Back-fill existing rows: completed participations (date_fin IS NOT NULL) -> CORRIGE
UPDATE participer_exo
SET etat_soumission = 'CORRIGE'
WHERE date_fin IS NOT NULL AND note IS NOT NULL;

-- 4. Indexes
CREATE INDEX IF NOT EXISTS idx_exercises_programmer_type ON exercises_programmer(type_assignation);
CREATE INDEX IF NOT EXISTS idx_participer_exo_etat        ON participer_exo(etat_soumission);
