SET search_path TO ressources;

-- Add date_publication column to cours table
ALTER TABLE cours ADD COLUMN IF NOT EXISTS date_publication TIMESTAMP;

-- Backfill: courses already PUBLIE before this column existed have no real
-- publication date on record, so fall back to their creation date.
UPDATE cours SET date_publication = date_creation WHERE etat = 'PUBLIE' AND date_publication IS NULL;
