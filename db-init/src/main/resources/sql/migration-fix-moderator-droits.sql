-- Ensure every class moderator has an acceder entry
INSERT INTO acceder (utilisateur_id, classe_id)
SELECT moderator_id, id FROM classes WHERE moderator_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Ensure every class moderator has droit_publication (peut_publier=true, peut_moderer=true)
INSERT INTO droit_publication (utilisateur_id, classe_id, date_attribution, peut_publier, peut_moderer)
SELECT moderator_id, id, COALESCE(date_creation, NOW()), TRUE, TRUE
FROM classes WHERE moderator_id IS NOT NULL
ON CONFLICT DO NOTHING;
