-- ADMINISTRATORS
INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES
('550e8400-e29b-41d4-a716-446655440999', 'Admin', 'Super', 'admin@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456777', 'Admin Office', 'pepe', 'ACTIVE', TRUE),

-- TEST PROFESSOR (YOUR ACCOUNT)
('660e8400-e29b-41d4-a716-446655440999', 'Test', 'Professor', 'kemogneprince06@yahoo.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456789', 'Test Address', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluQGV4YW1wbGUuY29tIiwiaWF0IjoxNzQ0OTY3MzM1LCJleHAiOjE3NDQ5NjgyMzV9.NVeY4KP8KAM2Nh80NaFXYEJ4__ceTFOPQPe_pGryMQw', 'ACTIVE', FALSE);

-- REGULAR USERS
INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Dupont', 'Jean', 'jean.dupont@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456789', '123 Rue de Paris', 'abc123activationcode1', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440001', 'Martin', 'Marie', 'marie.martin@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456788', '456 Avenue de Lyon', 'abc123activationcode2', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440002', 'Durand', 'Pierre', 'pierre.durand@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456787', '789 Boulevard de Nice', 'abc123activationcode3', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440003', 'Lefevre', 'Sophie', 'sophie.lefevre@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456786', '101 Rue de Marseille', 'abc123activationcode4', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440004', 'Durand', 'Paul', 'paul.durand@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456785', '111 Rue de Lille', 'abc123activationcode5', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440007', 'Marie', 'Dupont', 'marie.dupont@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456789', '123 Rue de Paris', 'abc123activationcode6', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440008', 'Lucas', 'Martin', 'lucas.martin@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456788', '456 Avenue de Lyon', 'abc123activationcode7', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440009', 'Isabelle', 'Lefevre', 'isabelle.lefevre@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456787', '789 Boulevard de Nice', 'abc123activationcode8', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440010', 'Paul', 'Durand', 'paul.durand@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456786', '111 Rue de Lille', 'abc123activationcode9', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440200', 'Parent A', 'FirstNameA', 'parenta@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456780', 'AddressA', 'abc123activationcode10', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440201', 'Parent B', 'FirstNameB', 'parentb@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456781', 'AddressB', 'abc123activationcode11', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440300', 'Eleve A', 'Jean', 'jean.elevea@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456700', '10 Rue des Écoles', 'abc123activationcode12', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440301', 'Eleve B', 'Marie', 'marie.eleveb@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456701', '20 Rue des Lycées', 'abc123activationcode13', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440302', 'Eleve C', 'Paul', 'paul.elevec@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456702', '30 Boulevard Université', 'abc123activationcode14', 'ACTIVE', FALSE);

-- PROFESSORS
INSERT INTO ressources.professeurs (professeurs_id, cni_url_front, cni_url_back, selfie_url, matricule_professeur) VALUES
('550e8400-e29b-41d4-a716-446655440007', 'https://example.com/cni/marie_dupont_front.jpg', 'https://example.com/cni/marie_dupont_back.jpg', 'https://example.com/selfie/marie_dupont_selfie.jpg', 'PROF-2024-001'),
('550e8400-e29b-41d4-a716-446655440008', 'https://example.com/cni/lucas_martin_front.jpg', 'https://example.com/cni/lucas_martin_back.jpg', NULL, 'PROF-2024-002'),
('550e8400-e29b-41d4-a716-446655440009', 'https://example.com/cni/isabelle_lefevre_front.jpg', 'https://example.com/cni/isabelle_lefevre_back.jpg', 'https://example.com/selfie/isabelle_lefevre_selfie.jpg', NULL),
('660e8400-e29b-41d4-a716-446655440999', 'http://example.com/cni.jpg', 'http://example.com/cni-back.jpg', 'http://example.com/selfie.jpg', 'PROF-TEST-001');

-- PARENTS
INSERT INTO ressources.parents (parents_id) VALUES
('550e8400-e29b-41d4-a716-446655440200'),
('550e8400-e29b-41d4-a716-446655440201');

-- STUDENTS
INSERT INTO ressources.eleves (eleves_id, niveau) VALUES
('550e8400-e29b-41d4-a716-446655440300', '6eme'),
('550e8400-e29b-41d4-a716-446655440301', '5eme'),
('550e8400-e29b-41d4-a716-446655440302', '4eme');

-- TUTORS
INSERT INTO ressources.repetiteurs (repetiteurs_id, cni_url_front, cni_url_back, photo_full_picture, nom_classe) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'https://example.com/cni/paul_durand_front.jpg', 'https://example.com/cni/paul_durand_back.jpg', 'https://example.com/photos/paul_durand_full.jpg', 'Terminale B');

-- SCHOOLS
INSERT INTO ressources.etablissements (id, nom, localisation, pays, email, telephone, option_envoi_mail_classe, option_token_general, code_unique) VALUES
('550e8400-e29b-41d4-a716-446655440100', 'Etablissement A', 'Yaoundé', 'Cameroun', 'contact@etab-a.cm', '23712345678', TRUE, FALSE, TRUE),
('550e8400-e29b-41d4-a716-446655440101', 'Etablissement B', 'Douala', 'Cameroun', 'info@etab-b.cm', '23787654321', FALSE, TRUE, FALSE);

-- CLASSES (with moderators)
INSERT INTO ressources.classes (id, nom, niveau, date_creation, code_activation, etat, etablissement_id, moderator_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', 'Classe A', 'Niveau 1', '2024-11-28 10:00:00', '123456', 'ACTIF', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440007'),
('550e8400-e29b-41d4-a716-446655440401', 'Classe B', 'Niveau 2', '2024-11-28 11:00:00', '654321', 'ACTIF', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440008');

-- MODERATOR RELATIONSHIPS
INSERT INTO ressources.professeur_classes_moderees (professeur_id, classe_id) VALUES
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440400'),
('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440401');

-- CLASS MEMBERSHIPS
INSERT INTO ressources.classe_parents (classe_id, parent_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440200'),
('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440201');

INSERT INTO ressources.classe_eleves (classe_id, eleve_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440300'),
('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440301'),
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440302');

-- SUBJECTS
INSERT INTO ressources.matieres (id, nom, description, date_creation, etat) VALUES
('550e8400-e29b-41d4-a716-446655441000', 'MATHEMATIQUES', 'Cours de mathématiques avancées', '2024-01-01 09:00:00', 'ACTIF'),
('550e8400-e29b-41d4-a716-446655441001', 'SCIENCES', 'Sciences physiques et naturelles', '2024-01-01 09:00:00', 'ACTIF'),
('550e8400-e29b-41d4-a716-446655441002', 'HISTOIRE', 'Histoire générale et du Cameroun', '2024-01-01 09:00:00', 'ACTIF'),
('550e8400-e29b-41d4-a716-446655441003', 'GEOGRAPHIE', 'Géographie mondiale et régionale', '2024-01-01 09:00:00', 'ACTIF'),
('550e8400-e29b-41d4-a716-446655441004', 'LANGUE', 'Langues et littérature', '2024-01-01 09:00:00', 'ACTIF');

-- CLASS-SUBJECT ASSOCIATIONS
INSERT INTO ressources.classe_matieres (matiere_id, classe_id) VALUES
('550e8400-e29b-41d4-a716-446655441000', '550e8400-e29b-41d4-a716-446655440400'),
('550e8400-e29b-41d4-a716-446655441001', '550e8400-e29b-41d4-a716-446655440400'),
('550e8400-e29b-41d4-a716-446655441002', '550e8400-e29b-41d4-a716-446655440401'),
('550e8400-e29b-41d4-a716-446655441003', '550e8400-e29b-41d4-a716-446655440401');

-- PROFESSOR-SUBJECT ASSOCIATIONS
INSERT INTO ressources.professeur_matiere (professeur_id, matiere_id) VALUES
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655441000'),
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655441001'),
('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655441002'),
('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655441003'),
('550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655441004');

-- COMMUNICATION CHANNELS
INSERT INTO ressources.canaux (id, nom, description, professeur_id, classe_id) VALUES
('550e8400-e29b-41d4-a716-446655440500', 'Canal de Mathématiques', 'Canal dédié aux cours de mathématiques', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440400'),
('550e8400-e29b-41d4-a716-446655440501', 'Canal de Français', 'Canal dédié aux cours de français', '550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440401'),
('550e8400-e29b-41d4-a716-446655440502', 'Canal de Sciences', 'Canal dédié aux cours de sciences', '550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440400');

-- REJECTION REASONS
INSERT INTO ressources.motifs_rejet (id, code, descriptif, date_creation) VALUES
('550e8400-e29b-41d4-a716-446655440600', 'PHOTO_FLOU_RECTO', 'Photo recto de la CNI floue ou illisible', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440601', 'PHOTO_FLOU_VERSO', 'Photo verso de la CNI floue ou illisible', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440602', 'PHOTO_FLOU_SELFIE', 'Photo selfie floue ou illisible', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440603', 'PHOTO_INCOHERENTE_RECTO_VERSO', 'Incohérence entre les photos recto et verso de la CNI', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440604', 'PHOTO_INCOHERENTE_RECTO_SELFIE', 'Incohérence entre la photo recto de la CNI et le selfie', CURRENT_TIMESTAMP),
('550e8400-e29b-41d4-a716-446655440605', 'MATRICULE_INCORRECT', 'Matricule professeur incorrect ou invalide', CURRENT_TIMESTAMP);

INSERT INTO ressources.motifs_rejet_classe (id, code, descriptif, date_creation) VALUES
('660e8400-e29b-41d4-a716-446655440600', 'CLASSE_NOM_INVALIDE', 'Nom de classe invalide ou non conforme', CURRENT_TIMESTAMP),
('660e8400-e29b-41d4-a716-446655440601', 'CLASSE_NIVEAU_INVALIDE', 'Niveau de classe non reconnu ou invalide', CURRENT_TIMESTAMP),
('660e8400-e29b-41d4-a716-446655440602', 'CLASSE_ETABLISSEMENT_INCONNU', 'Établissement associé inconnu ou non valide', CURRENT_TIMESTAMP),
('660e8400-e29b-41d4-a716-446655440603', 'CLASSE_CAPACITE_DEPASSEE', 'Capacité maximale d''élèves dépassée', CURRENT_TIMESTAMP),
('660e8400-e29b-41d4-a716-446655440604', 'CLASSE_PROFS_INSUFFISANTS', 'Nombre insuffisant de professeurs assignés', CURRENT_TIMESTAMP),
('660e8400-e29b-41d4-a716-446655440605', 'CLASSE_DOUBLON', 'Classe déjà existante avec les mêmes caractéristiques', CURRENT_TIMESTAMP),
('660e8400-e29b-41d4-a716-446655440606', 'CLASSE_HORAIRE_CONFLIT', 'Conflit d''horaire avec une autre classe', CURRENT_TIMESTAMP),
('660e8400-e29b-41d4-a716-446655440607', 'CLASSE_MATIERES_MANQUANTES', 'Matériel ou matières manquantes pour ce niveau', CURRENT_TIMESTAMP);

-- EVENTS
INSERT INTO ressources.evenements (id, titre, description, lieu, etat, heure_debut, heure_fin, createur_id) VALUES
('550e8400-e29b-41d4-a716-446655442000', 'Réunion parents-professeurs', 'Réunion trimestrielle', 'Salle de réunion', 'PLANIFIE', '2024-12-15 14:00:00', '2024-12-15 16:00:00', '550e8400-e29b-41d4-a716-446655440007'),
('550e8400-e29b-41d4-a716-446655442001', 'Sortie pédagogique', 'Visite du musée national', 'Musée National', 'PLANIFIE', '2024-12-20 09:00:00', '2024-12-20 17:00:00', '550e8400-e29b-41d4-a716-446655440008');

-- EVENT PARTICIPANTS
INSERT INTO ressources.evenement_participants (evenement_id, utilisateur_id) VALUES
('550e8400-e29b-41d4-a716-446655442000', '550e8400-e29b-41d4-a716-446655440200'),
('550e8400-e29b-41d4-a716-446655442000', '550e8400-e29b-41d4-a716-446655440300'),
('550e8400-e29b-41d4-a716-446655442001', '550e8400-e29b-41d4-a716-446655440201'),
('550e8400-e29b-41d4-a716-446655442001', '550e8400-e29b-41d4-a716-446655440301');

-- MESSAGES
INSERT INTO ressources.messages (id, contenu, datecreation, datemodification, etat, expediteur_id) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'Bonjour, comment ça va?', '2023-10-01', '2023-10-01', 'envoyé', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440011', 'Réunion à 10h demain.', '2023-10-02', '2023-10-02', 'envoyé', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440012', 'Merci pour votre aide.', '2023-10-03', '2023-10-03', 'envoyé', '550e8400-e29b-41d4-a716-446655440002'),
('550e8400-e29b-41d4-a716-446655440013', 'Veuillez trouver ci-joint le document.', '2023-10-04', '2023-10-04', 'envoyé', '550e8400-e29b-41d4-a716-446655440003'),
('550e8400-e29b-41d4-a716-446655440014', 'Réunion à 15h après demain.', '2024-10-02', '2023-10-02', 'envoyé', '550e8400-e29b-41d4-a716-446655440001');

-- MESSAGE RECIPIENTS
INSERT INTO ressources.recevoir (message_id, utilisateur_id) VALUES
('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440003'),
('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440003'),
('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440002'),
('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440003'),
('550e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440002'),
('550e8400-e29b-41d4-a716-446655440014', '550e8400-e29b-41d4-a716-446655440000');

-- MEDIA
INSERT INTO ressources.media (id, bucket_name, content_type, file_name, file_path, file_size, file_type, media_type, owner_id, uploaded_date, evenement_id) VALUES
('550e8400-e29b-41d4-a716-446655443000', 'school-bucket', 'image/jpeg', 'math_course.jpg', 'courses/math/2024/math_course.jpg', 1024, 'IMAGE', 'COURSE_MATERIAL', '550e8400-e29b-41d4-a716-446655440007', CURRENT_TIMESTAMP, NULL),
('550e8400-e29b-41d4-a716-446655443001', 'school-bucket', 'application/pdf', 'history_syllabus.pdf', 'courses/history/2024/syllabus.pdf', 2048, 'DOCUMENT', 'SYLLABUS', '550e8400-e29b-41d4-a716-446655440008', CURRENT_TIMESTAMP, NULL),
('550e8400-e29b-41d4-a716-446655443002', 'events-bucket', 'image/png', 'event_poster.png', 'events/2024/parent_meeting.png', 3072, 'IMAGE', 'EVENT_MATERIAL', '550e8400-e29b-41d4-a716-446655440007', CURRENT_TIMESTAMP, '550e8400-e29b-41d4-a716-446655442000');

-- INTERACTIONS
INSERT INTO ressources.interactions (id, type, content, creation_date, niveau, created_by, event_id, message_id) VALUES
('550e8400-e29b-41d4-a716-446655444000', 'COMMENT', 'Excellent cours aujourd''hui!', CURRENT_TIMESTAMP, 'POSITIVE', '550e8400-e29b-41d4-a716-446655440300', NULL, NULL),
('550e8400-e29b-41d4-a716-446655444001', 'QUESTION', 'Quand sera disponible le prochain devoir?', CURRENT_TIMESTAMP, 'NEUTRAL', '550e8400-e29b-41d4-a716-446655440301', NULL, NULL),
('550e8400-e29b-41d4-a716-446655444002', 'FEEDBACK', 'La réunion était très informative', CURRENT_TIMESTAMP, 'POSITIVE', '550e8400-e29b-41d4-a716-446655440200', '550e8400-e29b-41d4-a716-446655442000', NULL);

-- AUTHENTICATION TOKENS
INSERT INTO ressources.refresh_tokens (token, expiry_date, utilisateur_id) VALUES
('eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDA5OTkiLCJpYXQiOjE2MTYyMzkwMjIsImV4cCI6MTYxNjMyNTQyMn0.4j5X9v2JwQ7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q', '2025-12-31 23:59:59', '550e8400-e29b-41d4-a716-446655440999'),
('eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2NjBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDA5OTkiLCJpYXQiOjE2MTYyMzkwMjIsImV4cCI6MTYxNjMyNTQyMn0.4j5X9v2JwQ7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q', '2025-12-31 23:59:59', '660e8400-e29b-41d4-a716-446655440999');