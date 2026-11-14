SET search_path TO ressources;

-- Trace du code (token) d'établissement fourni à la création de la classe.
-- '' = classe créée sans code (l'option « Token général » était désactivée à ce moment-là).
-- NULL = inconnu : classe créée avant l'ajout de cette trace.
ALTER TABLE classes ADD COLUMN IF NOT EXISTS code_etablissement_utilise VARCHAR(255);
