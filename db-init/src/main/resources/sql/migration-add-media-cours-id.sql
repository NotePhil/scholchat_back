ALTER TABLE ressources.media ADD COLUMN IF NOT EXISTS cours_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_media_cours_id ON ressources.media(cours_id);
