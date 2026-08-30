SET search_path TO ressources;

-- Allow a media (image or PDF) to be attached directly to a question,
-- mirroring the existing evenement_id / cours_id link columns.
ALTER TABLE media ADD COLUMN IF NOT EXISTS question_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_media_question_id ON media(question_id);

ALTER TABLE media
    ADD CONSTRAINT fk_media_question
    FOREIGN KEY (question_id) REFERENCES questions_reponses(id) ON DELETE CASCADE;
