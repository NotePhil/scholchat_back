-- Add creator_id column to classes table
ALTER TABLE classes ADD COLUMN IF NOT EXISTS creator_id VARCHAR(255);

-- Backfill: for existing classes, creator = moderator
UPDATE classes SET creator_id = moderator_id WHERE creator_id IS NULL AND moderator_id IS NOT NULL;
