-- Changeset 26-seed-exercises-cours : pas d'exercice ni de devoir sans cours.
-- Rattache les exercices programmés des données de démonstration (insérés par les changesets 2-data et
-- 9-sample-courses avec cours_id NULL) à un cours programmé dans leur classe. Ne touche QUE ces lignes de
-- démonstration, par leurs identifiants fixes, et seulement si elles sont encore sans cours.
-- Idempotent : rejouable sans effet (ON CONFLICT DO NOTHING / WHERE cours_id IS NULL / garde-fous EXISTS).
SET search_path TO ressources;

-- 1. « Demo Class 1 » n'a aucun cours programmé correspondant aux exercices d'histoire et de dissertation :
--    on y programme des cours de démonstration existants (même matière / même thème).
INSERT INTO ressources.cours_programmer (id, cours_id, date_cours_prevue, etat_cours_programme, classe_id, lieu,
                                         description, date_creation, professeur_id)
SELECT v.id, v.cours_id, v.date_prevue, 'PLANIFIE', v.classe_id, v.lieu, v.description, v.date_creation, v.professeur_id
FROM (VALUES
    ('cp-006', 'dddddddd-0004-0004-0004-000000000004', TIMESTAMP '2025-04-08 08:00:00',
     '550e8400-e29b-41d4-a716-446655440407', 'Salle 103', 'Seance Revolution francaise',
     TIMESTAMP '2025-03-01 08:00:00', '550e8400-e29b-41d4-a716-446655440007'),
    ('cp-007', 'dddddddd-0004-0004-0004-000000000003', TIMESTAMP '2025-03-28 08:00:00',
     '550e8400-e29b-41d4-a716-446655440407', 'Salle 202', 'Seance methode de la dissertation',
     TIMESTAMP '2025-03-01 09:00:00', '550e8400-e29b-41d4-a716-446655440007')
) AS v(id, cours_id, date_prevue, classe_id, lieu, description, date_creation, professeur_id)
WHERE EXISTS (SELECT 1 FROM ressources.cours c WHERE c.id = v.cours_id)
  AND EXISTS (SELECT 1 FROM ressources.classes cl WHERE cl.id = v.classe_id)
  AND EXISTS (SELECT 1 FROM ressources.professeurs p WHERE p.professeurs_id = v.professeur_id)
ON CONFLICT (id) DO NOTHING;

INSERT INTO ressources.cours_programmer_classes (cours_programmer_id, classe_id)
SELECT cp.id, cp.classe_id
FROM ressources.cours_programmer cp
WHERE cp.id IN ('cp-006', 'cp-007')
ON CONFLICT DO NOTHING;

-- 2. Bibliothèque : l'exercice source est lié à son cours (comme les autres exercices de démonstration).
INSERT INTO ressources.cours_exercises (exercise_id, cours_id)
SELECT v.exercise_id, v.cours_id
FROM (VALUES
    ('ffffffff-0006-0006-0006-000000000006', 'dddddddd-0004-0004-0004-000000000004'),
    ('ffffffff-0006-0006-0006-000000000007', 'dddddddd-0004-0004-0004-000000000003'),
    ('ffffffff-0006-0006-0006-000000000008', 'dddddddd-0004-0004-0004-000000000001')
) AS v(exercise_id, cours_id)
WHERE EXISTS (SELECT 1 FROM ressources.exercises e WHERE e.id = v.exercise_id)
  AND EXISTS (SELECT 1 FROM ressources.cours c WHERE c.id = v.cours_id)
ON CONFLICT DO NOTHING;

-- 3. Rattachement des programmations de démonstration à leur cours. Garde-fous : la ligne est encore sans cours,
--    le cours existe et il est programmé dans chacune des classes où la programmation est diffusée.
UPDATE ressources.exercises_programmer ep
SET cours_id = v.cours_id
FROM (VALUES
    ('exo-prog-001',                         'course-demo-001'),                       -- Classe B : Mathematiques - Les fractions
    ('ffffffff-0006-0006-0006-000000000001', 'dddddddd-0004-0004-0004-000000000001'),  -- Introduction aux equations
    ('ffffffff-0006-0006-0006-000000000002', 'dddddddd-0004-0004-0004-000000000002'),  -- Les forces en physique
    ('ffffffff-0006-0006-0006-000000000004', 'dddddddd-0004-0004-0004-000000000005'),  -- Cours prive : Trigonometrie
    ('ffffffff-0006-0006-0006-000000000005', 'dddddddd-0004-0004-0004-000000000006'),  -- Cours public : Statistiques
    ('ffffffff-0006-0006-0006-000000000006', 'dddddddd-0004-0004-0004-000000000004'),  -- La Revolution francaise (cp-006)
    ('ffffffff-0006-0006-0006-000000000007', 'dddddddd-0004-0004-0004-000000000003'),  -- La dissertation (cp-007)
    ('ffffffff-0006-0006-0006-000000000008', 'dddddddd-0004-0004-0004-000000000001')   -- Introduction aux equations
) AS v(ep_id, cours_id)
WHERE ep.id = v.ep_id
  AND ep.cours_id IS NULL
  AND EXISTS (SELECT 1 FROM ressources.cours c WHERE c.id = v.cours_id)
  AND NOT EXISTS (
      SELECT 1 FROM ressources.exercise_programmer_classes epc
      WHERE epc.exercise_programmer_id = ep.id
        AND NOT EXISTS (
            SELECT 1 FROM ressources.cours_programmer cp
            WHERE cp.cours_id = v.cours_id
              AND (cp.classe_id = epc.classe_id
                   OR EXISTS (SELECT 1 FROM ressources.cours_programmer_classes cpc
                              WHERE cpc.cours_programmer_id = cp.id AND cpc.classe_id = epc.classe_id))));
