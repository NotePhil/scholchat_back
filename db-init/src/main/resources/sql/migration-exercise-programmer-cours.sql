-- Exercice programmé rattaché à un cours (changeset 25-exercise-programmer-cours).
-- cours_id NULL = « Exercices généraux » (exercices programmés avant ce changeset, ou sans cours).
-- Le cours doit être programmé dans la classe de diffusion (contrôle applicatif, erreur 400
-- COURS_NON_PROGRAMME_DANS_CLASSE). Suppression du cours -> l'exercice redevient général (SET NULL).
-- Idempotent : rejouable sans effet (IF NOT EXISTS / vérification de la contrainte).
SET search_path TO ressources;

ALTER TABLE ressources.exercises_programmer ADD COLUMN IF NOT EXISTS cours_id VARCHAR(255);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints
                   WHERE constraint_schema = 'ressources' AND table_name = 'exercises_programmer'
                     AND constraint_name = 'fk_exercises_programmer_cours') THEN
        ALTER TABLE ressources.exercises_programmer
            ADD CONSTRAINT fk_exercises_programmer_cours
            FOREIGN KEY (cours_id) REFERENCES ressources.cours(id) ON DELETE SET NULL;
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_exercises_programmer_cours ON ressources.exercises_programmer(cours_id);

-- Index des requêtes d'agrégation (résumé des cours d'une classe, progression, statistiques)
CREATE INDEX IF NOT EXISTS idx_cours_programmer_classes_classe ON ressources.cours_programmer_classes(classe_id);
CREATE INDEX IF NOT EXISTS idx_questions_reponses_exercise ON ressources.questions_reponses(exercise_id);
