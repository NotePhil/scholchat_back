SET search_path TO ressources;

-- Ensure every class moderator has an acceder entry
INSERT INTO acceder (utilisateur_id, classe_id)
SELECT moderator_id, id FROM classes WHERE moderator_id IS NOT NULL
ON CONFLICT DO NOTHING;
