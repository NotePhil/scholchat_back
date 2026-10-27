SET search_path TO ressources;

-- Date de PUBLICATION d'un événement (création du post dans le fil d'actualité), distincte de
-- heure_debut (début de l'événement). Stockée en UTC comme le reste du serveur.
ALTER TABLE evenements ADD COLUMN IF NOT EXISTS date_publication TIMESTAMP;

-- Rattrapage des événements existants (aucun horodatage de création n'était enregistré) : la
-- publication précède forcément le premier média téléversé, la première interaction (like,
-- commentaire…) et — en pratique — le début de l'événement ; et jamais dans le futur.
-- LEAST ignore les NULL.
UPDATE evenements e
SET date_publication = LEAST(
        (SELECT MIN(m.uploaded_date) FROM media m WHERE m.evenement_id = e.id),
        (SELECT MIN(i.creation_date) FROM interactions i WHERE i.event_id = e.id),
        e.heure_debut,
        (now() AT TIME ZONE 'UTC'))
WHERE e.date_publication IS NULL;

ALTER TABLE evenements ALTER COLUMN date_publication SET DEFAULT (now() AT TIME ZONE 'UTC');
