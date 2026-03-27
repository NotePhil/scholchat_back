SET search_path TO ressources;

-- Sample course created by professor (Prof Math - 550e8400-e29b-41d4-a716-446655440007)
-- Linked to Demo Class 1 (550e8400-e29b-41d4-a716-446655440401)
INSERT INTO cours (id, titre, description, date_creation, etat, restriction, redacteur_id)
VALUES
  ('course-demo-001', 'Mathematiques - Les fractions', 'Cours sur les fractions: addition, soustraction, multiplication et division de fractions.', NOW(), 'PUBLIE', 'PUBLIC', '550e8400-e29b-41d4-a716-446655440007'),
  ('course-demo-002', 'Francais - La conjugaison', 'Les temps de conjugaison: present, passe compose, imparfait, futur simple.', NOW(), 'PUBLIE', 'PUBLIC', '550e8400-e29b-41d4-a716-446655440007')
ON CONFLICT (id) DO NOTHING;

-- Link courses to matieres
INSERT INTO cours_matiere (cours_id, matiere_id)
VALUES
  ('course-demo-001', '550e8400-e29b-41d4-a716-446655440501'),
  ('course-demo-002', '550e8400-e29b-41d4-a716-446655440502')
ON CONFLICT DO NOTHING;

-- Schedule courses to Demo Class 1
INSERT INTO cours_programmer (id, source_exercise_id, date_exo_prevue, date_debut_exo_effectif, date_fin_exo_effectif, etat_exercise_programmer, programme_par_id)
VALUES
  ('sched-course-001', 'course-demo-001', NOW() + INTERVAL '7 days', NULL, NULL, 'PLANIFIE', '550e8400-e29b-41d4-a716-446655440007'),
  ('sched-course-002', 'course-demo-002', NOW() + INTERVAL '14 days', NULL, NULL, 'PLANIFIE', '550e8400-e29b-41d4-a716-446655440007')
ON CONFLICT (id) DO NOTHING;

-- Link scheduled courses to class
INSERT INTO cours_programmer_classes (cours_programmer_id, classe_id)
VALUES
  ('sched-course-001', '550e8400-e29b-41d4-a716-446655440401'),
  ('sched-course-002', '550e8400-e29b-41d4-a716-446655440401')
ON CONFLICT DO NOTHING;

-- Sample exercise created by professor
INSERT INTO exercises (id, nom, description, date_creation, etat, restriction, niveau, redacteur_id)
VALUES
  ('exercise-demo-001', 'Quiz Mathematiques - Fractions', 'Testez vos connaissances sur les fractions avec ce quiz de 5 questions.', NOW(), 'PUBLIE', 'PUBLIC', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440007')
ON CONFLICT (id) DO NOTHING;

-- Link exercise to course
INSERT INTO cours_exercises (cours_id, exercise_id)
VALUES ('course-demo-001', 'exercise-demo-001')
ON CONFLICT DO NOTHING;

-- Questions for the exercise
INSERT INTO questions_reponses (id, intitule, reponse, type_question, points, exercise_id)
VALUES
  ('question-001', 'Combien fait 1/2 + 1/4 ?', '3/4', 'QCM', 2, 'exercise-demo-001'),
  ('question-002', 'Vrai ou Faux: 2/3 est plus grand que 3/4', 'Faux', 'VRAI_FAUX', 1, 'exercise-demo-001'),
  ('question-003', 'Simplifiez la fraction 6/8', '3/4', 'REPONSE_COURTE', 2, 'exercise-demo-001'),
  ('question-004', 'Combien fait 3/5 x 2/3 ?', '2/5', 'QCM', 2, 'exercise-demo-001'),
  ('question-005', 'Expliquez comment additionner deux fractions avec des denominateurs differents.', 'Il faut trouver un denominateur commun, puis additionner les numerateurs.', 'REPONSE_LONGUE', 3, 'exercise-demo-001')
ON CONFLICT (id) DO NOTHING;

-- MCQ choices for question 1
INSERT INTO choix_reponses (id, texte, est_correct, ordre_affichage, question_id)
VALUES
  ('choice-001-a', '1/2', false, 1, 'question-001'),
  ('choice-001-b', '2/4', false, 2, 'question-001'),
  ('choice-001-c', '3/4', true, 3, 'question-001'),
  ('choice-001-d', '1/6', false, 4, 'question-001')
ON CONFLICT (id) DO NOTHING;

-- MCQ choices for question 4
INSERT INTO choix_reponses (id, texte, est_correct, ordre_affichage, question_id)
VALUES
  ('choice-004-a', '6/15', false, 1, 'question-004'),
  ('choice-004-b', '2/5', true, 2, 'question-004'),
  ('choice-004-c', '5/8', false, 3, 'question-004'),
  ('choice-004-d', '1/3', false, 4, 'question-004')
ON CONFLICT (id) DO NOTHING;

-- Schedule the exercise to Demo Class 1
INSERT INTO exercises_programmer (id, source_exercise_id, date_exo_prevue, date_debut_exo_effectif, date_fin_exo_effectif, etat_exercise_programmer, programme_par_id)
VALUES
  ('exo-prog-001', 'exercise-demo-001', NOW() + INTERVAL '3 days', NOW(), NOW() + INTERVAL '30 days', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007')
ON CONFLICT (id) DO NOTHING;

-- Link exercise programmer to class
INSERT INTO exercise_programmer_classes (exercise_programmer_id, classe_id)
VALUES ('exo-prog-001', '550e8400-e29b-41d4-a716-446655440401')
ON CONFLICT DO NOTHING;
