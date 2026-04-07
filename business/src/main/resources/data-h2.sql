INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES
-- Add admin user
('550e8400-e29b-41d4-a716-446655440999', 'Admin', 'Super', 'admin@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456777', 'Admin Office', 'pepe', 'ACTIVE', TRUE),

-- Regular users (existing data)
('550e8400-e29b-41d4-a716-446655440000', 'Dupont', 'Jean', 'kemogneprince05@gmail.com', 'password123', '0123456789', '123 Rue de Paris', 'abc123activationcode1', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440001', 'Martin', 'Marie', 'marie.martin@example.com', 'password123', '0123456788', '456 Avenue de Lyon', 'abc123activationcode2', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440002', 'Durand', 'Pierre', 'pierre.durand@example.com', 'password123', '0123456787', '789 Boulevard de Nice', 'abc123activationcode3', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440003', 'Lefevre', 'Sophie', 'sophie.lefevre@example.com', 'password123', '0123456786', '101 Rue de Marseille', 'abc123activationcode4', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440004', 'Durand', 'Paul', 'peroldkamsu83@gmail.com', 'password123', '0123456785', '111 Rue de Lille', 'abc123activationcode5', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440007', 'Marie', 'Dupont', 'kpgpa237@gmail.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456789', '123 Rue de Paris', 'abc123activationcode6', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440008', 'Lucas', 'Martin', 'peroldkamsu33@gmail.com', 'password123', '0123456788', '456 Avenue de Lyon', 'abc123activationcode7', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440009', 'Isabelle', 'Lefevre', 'isabelle.lefevre@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456787', '789 Boulevard de Nice', 'abc123activationcode8', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440010', 'Paul', 'Durand', 'paul.durand@example.com', 'password123', '0123456786', '111 Rue de Lille', 'abc123activationcode9', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440200', 'Parent A', 'FirstNameA', 'peroldkamsu38@gmail.com', 'password123', '0123456780', 'AddressA', 'abc123activationcode10', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440201', 'Parent B', 'FirstNameB', 'parentb@example.com', 'password123', '0123456781', 'AddressB', 'abc123activationcode11', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440300', 'Eleve A', 'Jean', 'jean.elevea@example.com', 'password123', '0123456700', '10 Rue des Écoles', 'abc123activationcode12', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440301', 'Eleve B', 'Marie', 'marie.eleveb@example.com', 'password123', '0123456701', '20 Rue des Lycées', 'abc123activationcode13', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440302', 'Eleve C', 'Paul', 'paul.elevec@example.com', 'password123', '0123456702', '30 Boulevard Université', 'abc123activationcode14', 'ACTIVE', FALSE),
('660e8400-e29b-41d4-a716-446655440999', 'Test', 'Professor', 'ulrich@gmail.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456789', 'Test Address', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluQGV4YW1wbGUuY29tIiwiaWF0IjoxNzQ0OTY3MzM1LCJleHAiOjE3NDQ5NjgyMzV9.NVeY4KP8KAM2Nh80NaFXYEJ4__ceTFOPQPe_pGryMQw', 'AWAITING_VALIDATION', FALSE);

-- Insert schools (updated schema)
INSERT INTO ressources.etablissements (id, nom, localisation, pays, email, telephone, option_envoi_mail_new_classe, option_token_general, code_unique, gestionnaire_id) VALUES
('550e8400-e29b-41d4-a716-446655440100', 'École Email Approval', 'Yaoundé', 'Cameroun', 'contact@etab-a.cm', '23712345678', TRUE, FALSE, 'ETB-12345678', '550e8400-e29b-41d4-a716-446655440999'),
('550e8400-e29b-41d4-a716-446655440101', 'Lycée Token General', 'Douala', 'Cameroun', 'info@etab-b.cm', '23787654321', TRUE, TRUE, 'ETB-87654321', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440102', 'Collège Code Unique', 'Bafoussam', 'Cameroun', 'admin@college-c.cm', '23798765432', FALSE, TRUE, 'ETB-11223344', '550e8400-e29b-41d4-a716-446655440007');

-- Insert professors (must come after users)
INSERT INTO ressources.professeurs (professeurs_id, cni_url_front, cni_url_back, selfie_url, matricule_professeur, has_uploaded) VALUES
('550e8400-e29b-41d4-a716-446655440007', 'https://example.com/cni/marie_dupont_front.jpg', 'https://example.com/cni/marie_dupont_back.jpg', 'https://example.com/selfie/marie_dupont_selfie.jpg', 'PROF-2024-001', true),
('550e8400-e29b-41d4-a716-446655440008', 'https://example.com/cni/lucas_martin_front.jpg', 'https://example.com/cni/lucas_martin_back.jpg', NULL, 'PROF-2024-002', true),
('550e8400-e29b-41d4-a716-446655440009', 'https://example.com/cni/isabelle_lefevre_front.jpg', 'https://example.com/cni/isabelle_lefevre_back.jpg', 'https://example.com/selfie/isabelle_lefevre_selfie.jpg', NULL, true),
('660e8400-e29b-41d4-a716-446655440999', 'http://example.com/cni.jpg', 'http://example.com/cni-back.jpg', 'http://example.com/selfie.jpg', 'PROF-TEST-001', true);

-- Insert parents (must come after users)
INSERT INTO ressources.parents (parents_id) VALUES
('550e8400-e29b-41d4-a716-446655440200'),
('550e8400-e29b-41d4-a716-446655440201');

-- Insert students (must come after users)
INSERT INTO ressources.eleves (eleves_id, niveau) VALUES
('550e8400-e29b-41d4-a716-446655440300', '6eme'),
('550e8400-e29b-41d4-a716-446655440301', '5eme'),
('550e8400-e29b-41d4-a716-446655440302', '3eme');

-- Insert tutors (must come after users)
INSERT INTO ressources.repetiteurs (repetiteurs_id, cni_url_front, cni_url_back, photo_full_picture, nom_classe) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'https://example.com/cni/paul_durand_front.jpg', 'https://example.com/cni/paul_durand_back.jpg', 'https://example.com/photos/paul_durand_full.jpg', 'Terminale B');

-- Insert classes (must come after schools and professors)
INSERT INTO ressources.classes (id, nom, niveau, date_creation, code_activation, etat, etablissement_id, moderator_id, acces_majeur, payment_required) VALUES
('550e8400-e29b-41d4-a716-446655440400', 'Classe A', '3ème', '2024-11-28 08:00:00', '123456', 'ACTIF', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440007', FALSE, FALSE),
('550e8400-e29b-41d4-a716-446655440401', 'Classe B', '2nde', '2024-11-28 09:00:00', '234567', 'ACTIF', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440008', TRUE, FALSE),
('550e8400-e29b-41d4-a716-446655440402', 'Classe C - Pending', '4ème', '2024-11-29 09:00:00', '789012', 'EN_ATTENTE_APPROBATION', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440009', FALSE, FALSE),
('550e8400-e29b-41d4-a716-446655440403', 'Classe D - Pending', '1ère', '2024-11-29 10:00:00', '890123', 'EN_ATTENTE_APPROBATION', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440008', TRUE, FALSE),
('550e8400-e29b-41d4-a716-446655440404', 'Classe E - Inactive', '4ème', '2024-11-30 08:00:00', '901234', 'INACTIF', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440007', FALSE, FALSE),
('550e8400-e29b-41d4-a716-446655440405', 'Classe F - Inactive', 'Terminale', '2024-11-30 09:00:00', '012345', 'INACTIF', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440008', FALSE, FALSE),
('550e8400-e29b-41d4-a716-446655440406', 'Independent Class', 'CE1', '2024-12-01 10:00:00', '567890', 'EN_ATTENTE_APPROBATION', NULL, NULL, FALSE, TRUE),
-- Demo classes for kpgpa237@gmail.com with publication rights
('550e8400-e29b-41d4-a716-446655440407', 'Demo Class 1', '6ème', '2024-12-01 11:00:00', '111111', 'ACTIF', '550e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440007', TRUE, FALSE),
('550e8400-e29b-41d4-a716-446655440408', 'Demo Class 2', '5ème', '2024-12-01 12:00:00', '222222', 'ACTIF', '550e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440007', TRUE, FALSE);

-- Insert class relationships
INSERT INTO ressources.classe_parents (classe_id, parent_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440200'),
('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440201'),
-- Associate parents to demo classes
('550e8400-e29b-41d4-a716-446655440407', '550e8400-e29b-41d4-a716-446655440200'),
('550e8400-e29b-41d4-a716-446655440408', '550e8400-e29b-41d4-a716-446655440201');

INSERT INTO ressources.classe_eleves (classe_id, eleve_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440300'),
('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440301'),
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440302'),
-- Associate students to demo classes
('550e8400-e29b-41d4-a716-446655440407', '550e8400-e29b-41d4-a716-446655440300'),
('550e8400-e29b-41d4-a716-446655440408', '550e8400-e29b-41d4-a716-446655440301');

-- Insert moderator relationships
INSERT INTO ressources.professeur_classes_moderees (professeur_id, classe_id) VALUES
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440400'),
('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440401'),
('550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440402'),
('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440403'),
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440404'),
('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440405'),
-- Demo classes moderated by kpgpa237@gmail.com
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440407'),
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440408');

-- Insert publication rights
INSERT INTO ressources.droit_publication (utilisateur_id, classe_id, date_attribution, peut_publier, peut_moderer) VALUES
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00', TRUE, TRUE),
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440408', '2024-12-01 12:00:00', TRUE, TRUE);

-- Insert access rights for demo classes
INSERT INTO ressources.acceder (utilisateur_id, classe_id, date_acces) VALUES
-- Professor access
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00'),
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440408', '2024-12-01 12:00:00'),
-- Student access
('550e8400-e29b-41d4-a716-446655440300', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00'),
('550e8400-e29b-41d4-a716-446655440301', '550e8400-e29b-41d4-a716-446655440408', '2024-12-01 12:00:00'),
-- Parent access
('550e8400-e29b-41d4-a716-446655440200', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00'),
('550e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440408', '2024-12-01 12:00:00'),
-- Additional users access to Demo Class 1
('550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00'),
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00');

-- =============================================
-- MATIERES
-- =============================================
INSERT INTO ressources.matieres (id, nom, description, etat) VALUES
('aaaaaaaa-0001-0001-0001-000000000001', 'Mathematiques', 'Algebre, geometrie et analyse', 'ACTIF'),
('aaaaaaaa-0001-0001-0001-000000000002', 'Physique-Chimie', 'Sciences physiques et chimiques', 'ACTIF'),
('aaaaaaaa-0001-0001-0001-000000000003', 'Francais', 'Langue et litterature francaise', 'ACTIF'),
('aaaaaaaa-0001-0001-0001-000000000004', 'Histoire-Geographie', 'Histoire et geographie', 'ACTIF'),
('aaaaaaaa-0001-0001-0001-000000000005', 'SVT', 'Sciences de la vie et de la terre', 'ACTIF');

-- =============================================
-- PROFESSEUR - MATIERE
-- =============================================
INSERT INTO ressources.professeur_matiere (professeur_id, matiere_id) VALUES
('550e8400-e29b-41d4-a716-446655440007', 'aaaaaaaa-0001-0001-0001-000000000001'),
('550e8400-e29b-41d4-a716-446655440007', 'aaaaaaaa-0001-0001-0001-000000000002'),
('550e8400-e29b-41d4-a716-446655440008', 'aaaaaaaa-0001-0001-0001-000000000003'),
('550e8400-e29b-41d4-a716-446655440009', 'aaaaaaaa-0001-0001-0001-000000000004'),
('660e8400-e29b-41d4-a716-446655440999', 'aaaaaaaa-0001-0001-0001-000000000005');

-- =============================================
-- CLASSE - MATIERES
-- =============================================
INSERT INTO ressources.classe_matieres (matiere_id, classe_id) VALUES
('aaaaaaaa-0001-0001-0001-000000000001', '550e8400-e29b-41d4-a716-446655440400'),
('aaaaaaaa-0001-0001-0001-000000000002', '550e8400-e29b-41d4-a716-446655440400'),
('aaaaaaaa-0001-0001-0001-000000000003', '550e8400-e29b-41d4-a716-446655440401'),
('aaaaaaaa-0001-0001-0001-000000000001', '550e8400-e29b-41d4-a716-446655440407'),
('aaaaaaaa-0001-0001-0001-000000000002', '550e8400-e29b-41d4-a716-446655440407'),
('aaaaaaaa-0001-0001-0001-000000000003', '550e8400-e29b-41d4-a716-446655440408'),
('aaaaaaaa-0001-0001-0001-000000000004', '550e8400-e29b-41d4-a716-446655440408');

-- =============================================
-- PARENT - ELEVE
-- =============================================
INSERT INTO ressources.parent_eleve (parent_id, eleve_id) VALUES
('550e8400-e29b-41d4-a716-446655440200', '550e8400-e29b-41d4-a716-446655440300'),
('550e8400-e29b-41d4-a716-446655440200', '550e8400-e29b-41d4-a716-446655440302'),
('550e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440301');

-- =============================================
-- CANAUX
-- =============================================
INSERT INTO ressources.canaux (id, nom, description, professeur_id, classe_id) VALUES
('bbbbbbbb-0002-0002-0002-000000000001', 'General Classe A', 'Canal principal de la Classe A', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440400'),
('bbbbbbbb-0002-0002-0002-000000000002', 'Mathematiques Demo1', 'Canal maths Demo Class 1', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440407'),
('bbbbbbbb-0002-0002-0002-000000000003', 'Physique Demo1', 'Canal physique Demo Class 1', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440407'),
('bbbbbbbb-0002-0002-0002-000000000004', 'Francais Classe B', 'Canal francais Classe B', '550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440401');

-- =============================================
-- MESSAGES
-- =============================================
INSERT INTO ressources.messages (id, objet, contenu, datecreation, etat, expediteur_id, deleted) VALUES
('msg-001', 'Bienvenue', 'Bienvenue dans la classe !', '2024-12-02 08:00:00', 'ENVOYE', '550e8400-e29b-41d4-a716-446655440007', FALSE),
('msg-002', 'Devoir', 'Le devoir est pour vendredi.', '2024-12-03 09:00:00', 'ENVOYE', '550e8400-e29b-41d4-a716-446655440008', FALSE),
('msg-003', 'Rappel', 'Rappel : reunion parents demain.', '2024-12-04 10:00:00', 'ENVOYE', '550e8400-e29b-41d4-a716-446655440007', FALSE);

INSERT INTO ressources.recevoir (message_id, utilisateur_id) VALUES
('msg-001', '550e8400-e29b-41d4-a716-446655440300'),
('msg-001', '550e8400-e29b-41d4-a716-446655440301'),
('msg-002', '550e8400-e29b-41d4-a716-446655440300'),
('msg-003', '550e8400-e29b-41d4-a716-446655440200');

INSERT INTO ressources.message_classes (message_id, classe_id) VALUES
('msg-001', '550e8400-e29b-41d4-a716-446655440407'),
('msg-002', '550e8400-e29b-41d4-a716-446655440401'),
('msg-003', '550e8400-e29b-41d4-a716-446655440407');

-- =============================================
-- EVENEMENTS (createur_id -> professeurs FK)
-- =============================================
INSERT INTO ressources.evenements (id, titre, description, lieu, etat, heure_debut, heure_fin, createur_id) VALUES
('cccccccc-0003-0003-0003-000000000001', 'Reunion parents-professeurs', 'Reunion trimestrielle', 'Salle A', 'PLANIFIE', '2025-01-15 17:00:00', '2025-01-15 19:00:00', '550e8400-e29b-41d4-a716-446655440007'),
('cccccccc-0003-0003-0003-000000000002', 'Sortie scolaire', 'Visite du musee national', 'Musee National', 'PLANIFIE', '2025-02-10 08:00:00', '2025-02-10 17:00:00', '550e8400-e29b-41d4-a716-446655440008'),
('cccccccc-0003-0003-0003-000000000003', 'Concours de maths', 'Competition inter-classes', 'Amphi', 'PASSE', '2024-12-05 09:00:00', '2024-12-05 12:00:00', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO ressources.evenement_participants (evenement_id, utilisateur_id) VALUES
('cccccccc-0003-0003-0003-000000000001', '550e8400-e29b-41d4-a716-446655440200'),
('cccccccc-0003-0003-0003-000000000001', '550e8400-e29b-41d4-a716-446655440201'),
('cccccccc-0003-0003-0003-000000000001', '550e8400-e29b-41d4-a716-446655440007'),
('cccccccc-0003-0003-0003-000000000002', '550e8400-e29b-41d4-a716-446655440300'),
('cccccccc-0003-0003-0003-000000000002', '550e8400-e29b-41d4-a716-446655440301'),
('cccccccc-0003-0003-0003-000000000003', '550e8400-e29b-41d4-a716-446655440300'),
('cccccccc-0003-0003-0003-000000000003', '550e8400-e29b-41d4-a716-446655440302');

-- =============================================
-- COURS (contenu NOT NULL relaxed by ALTER TABLE later in schema)
-- =============================================
INSERT INTO ressources.cours (id, titre, description, date_creation, etat, contenu, redacteur_id) VALUES
('dddddddd-0004-0004-0004-000000000001', 'Introduction aux equations', 'Bases des equations du 1er degre', '2024-12-01 08:00:00', 'PUBLIE', 'Une equation est une egalite comportant une inconnue.', '550e8400-e29b-41d4-a716-446655440007'),
('dddddddd-0004-0004-0004-000000000002', 'Les forces en physique', 'Newton et les lois du mouvement', '2024-12-02 08:00:00', 'PUBLIE', 'Une force est une action mecanique exercee sur un corps.', '550e8400-e29b-41d4-a716-446655440007'),
('dddddddd-0004-0004-0004-000000000003', 'La dissertation francaise', 'Methodologie de la dissertation', '2024-12-03 08:00:00', 'BROUILLON', 'La dissertation est un exercice de reflexion structuree.', '550e8400-e29b-41d4-a716-446655440008'),
('dddddddd-0004-0004-0004-000000000004', 'La Revolution francaise', 'Causes et consequences', '2024-12-04 08:00:00', 'PUBLIE', 'La Revolution francaise debuta en 1789.', '550e8400-e29b-41d4-a716-446655440009');

INSERT INTO ressources.cours_matiere (cours_id, matiere_id, ordre_dans_cours) VALUES
('dddddddd-0004-0004-0004-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001', 1),
('dddddddd-0004-0004-0004-000000000002', 'aaaaaaaa-0001-0001-0001-000000000002', 1),
('dddddddd-0004-0004-0004-000000000003', 'aaaaaaaa-0001-0001-0001-000000000003', 1),
('dddddddd-0004-0004-0004-000000000004', 'aaaaaaaa-0001-0001-0001-000000000004', 1);

-- =============================================
-- CHAPITRES
-- =============================================
INSERT INTO ressources.chapitres (id, titre, description, ordre, contenu, cours_id) VALUES
('eeeeeeee-0005-0005-0005-000000000001', 'Definition et vocabulaire', 'Vocabulaire de base', 1, 'Une equation est composee de deux membres.', 'dddddddd-0004-0004-0004-000000000001'),
('eeeeeeee-0005-0005-0005-000000000002', 'Resolution graphique', 'Methode graphique', 2, 'On peut resoudre une equation graphiquement.', 'dddddddd-0004-0004-0004-000000000001'),
('eeeeeeee-0005-0005-0005-000000000003', 'Premiere loi de Newton', 'Principe inertie', 1, 'Tout corps persevere dans son etat de repos.', 'dddddddd-0004-0004-0004-000000000002'),
('eeeeeeee-0005-0005-0005-000000000004', 'Deuxieme loi de Newton', 'Principe fondamental', 2, 'La somme des forces est egale a m*a.', 'dddddddd-0004-0004-0004-000000000002'),
('eeeeeeee-0005-0005-0005-000000000005', 'Introduction et plan', 'Structure de la dissertation', 1, 'Une dissertation comporte trois parties.', 'dddddddd-0004-0004-0004-000000000003');

-- =============================================
-- EXERCISES
-- =============================================
INSERT INTO ressources.exercises (id, nom, description, date_creation, etat, restriction, niveau, redacteur_id) VALUES
('ffffffff-0006-0006-0006-000000000001', 'Exercice equations 3eme', 'Resoudre des equations simples', '2024-12-05 08:00:00', 'PUBLIE', 'PUBLIC', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440007'),
('ffffffff-0006-0006-0006-000000000002', 'QCM Forces', 'Questions sur les forces', '2024-12-06 08:00:00', 'PUBLIE', 'PUBLIC', 'LYCEE', '550e8400-e29b-41d4-a716-446655440007'),
('ffffffff-0006-0006-0006-000000000003', 'Analyse de texte', 'Exercice de comprehension', '2024-12-07 08:00:00', 'PUBLIE', 'PRIVE', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440008');

INSERT INTO ressources.exercise_matieres (exercise_id, matiere_id) VALUES
('ffffffff-0006-0006-0006-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001'),
('ffffffff-0006-0006-0006-000000000002', 'aaaaaaaa-0001-0001-0001-000000000002'),
('ffffffff-0006-0006-0006-000000000003', 'aaaaaaaa-0001-0001-0001-000000000003');

INSERT INTO ressources.cours_exercises (exercise_id, cours_id) VALUES
('ffffffff-0006-0006-0006-000000000001', 'dddddddd-0004-0004-0004-000000000001'),
('ffffffff-0006-0006-0006-000000000002', 'dddddddd-0004-0004-0004-000000000002'),
('ffffffff-0006-0006-0006-000000000003', 'dddddddd-0004-0004-0004-000000000003');

-- =============================================
-- QUESTIONS REPONSES
-- =============================================
INSERT INTO ressources.questions_reponses (id, intitule, reponse, type_question, exercise_id) VALUES
('11111111-0007-0007-0007-000000000001', 'Resoudre : 2x + 3 = 7', 'x = 2', 'REPONSE_COURTE', 'ffffffff-0006-0006-0006-000000000001'),
('11111111-0007-0007-0007-000000000002', 'Resoudre : 5x - 10 = 0', 'x = 2', 'REPONSE_COURTE', 'ffffffff-0006-0006-0006-000000000001'),
('11111111-0007-0007-0007-000000000003', 'Quelle loi enonce que F = ma ?', 'La deuxieme loi de Newton', 'QCM', 'ffffffff-0006-0006-0006-000000000002'),
('11111111-0007-0007-0007-000000000004', 'L inertie est decrite par quelle loi ?', 'La premiere loi de Newton', 'QCM', 'ffffffff-0006-0006-0006-000000000002'),
('11111111-0007-0007-0007-000000000005', 'Quel est le theme principal du texte ?', NULL, 'REPONSE_LONGUE', 'ffffffff-0006-0006-0006-000000000003');

-- =============================================
-- COURS PROGRAMMER (professeur_id added  by ALTER TABLE in schema)
-- =============================================
INSERT INTO ressources.cours_programmer (id, cours_id, date_cours_prevue, etat_cours_programme, classe_id, lieu, description, date_creation, professeur_id) VALUES
('cp-001', 'dddddddd-0004-0004-0004-000000000001', '2025-01-10 08:00:00', 'PLANIFIE', '550e8400-e29b-41d4-a716-446655440407', 'Salle 101', 'Seance 1 equations', '2024-12-10 08:00:00', '550e8400-e29b-41d4-a716-446655440007'),
('cp-002', 'dddddddd-0004-0004-0004-000000000002', '2025-01-12 10:00:00', 'PLANIFIE', '550e8400-e29b-41d4-a716-446655440407', 'Labo Physique', 'Seance forces', '2024-12-10 09:00:00', '550e8400-e29b-41d4-a716-446655440007'),
('cp-003', 'dddddddd-0004-0004-0004-000000000003', '2025-01-14 08:00:00', 'TERMINE', '550e8400-e29b-41d4-a716-446655440401', 'Salle 202', 'Seance dissertation', '2024-12-11 08:00:00', '550e8400-e29b-41d4-a716-446655440008');

INSERT INTO ressources.cours_programmer_classes (cours_programmer_id, classe_id) VALUES
('cp-001', '550e8400-e29b-41d4-a716-446655440407'),
('cp-002', '550e8400-e29b-41d4-a716-446655440407'),
('cp-003', '550e8400-e29b-41d4-a716-446655440401');

INSERT INTO ressources.cours_programmer_participants (cours_programmer_id, utilisateur_id) VALUES
('cp-001', '550e8400-e29b-41d4-a716-446655440300'),
('cp-001', '550e8400-e29b-41d4-a716-446655440301'),
('cp-002', '550e8400-e29b-41d4-a716-446655440300'),
('cp-003', '550e8400-e29b-41d4-a716-446655440301');

-- =============================================
-- EXERCISES PROGRAMMER (exercise_id is PK in H2 schema)
-- =============================================
INSERT INTO ressources.exercises_programmer (exercise_id, date_exo_prevue, date_debut_exo_effectif, date_fin_exo_effectif, etat_exercise_programmer, programme_par_id) VALUES
('ffffffff-0006-0006-0006-000000000001', '2025-01-15 08:00:00', '2025-01-15 08:00:00', '2025-01-15 09:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007'),
('ffffffff-0006-0006-0006-000000000002', '2025-01-17 10:00:00', '2025-01-17 10:00:00', '2025-01-17 11:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO ressources.exercise_programmer_classes (exercise_programmer_id, classe_id) VALUES
('ffffffff-0006-0006-0006-000000000001', '550e8400-e29b-41d4-a716-446655440407'),
('ffffffff-0006-0006-0006-000000000002', '550e8400-e29b-41d4-a716-446655440407');

INSERT INTO ressources.participer_exo (utilisateur_id, exercise_programmer_id, note, appreciation, date_debut, date_fin) VALUES
('550e8400-e29b-41d4-a716-446655440300', 'ffffffff-0006-0006-0006-000000000001', '15', 'Bien', '2025-01-15 08:05:00', '2025-01-15 08:50:00'),
('550e8400-e29b-41d4-a716-446655440301', 'ffffffff-0006-0006-0006-000000000001', '12', 'Assez bien', '2025-01-15 08:10:00', '2025-01-15 08:55:00');

INSERT INTO ressources.repondre (utilisateur_id, question_id, reponse_utilisateur, est_correcte, note) VALUES
('550e8400-e29b-41d4-a716-446655440300', '11111111-0007-0007-0007-000000000001', 'x = 2', TRUE, '5'),
('550e8400-e29b-41d4-a716-446655440300', '11111111-0007-0007-0007-000000000002', 'x = 2', TRUE, '5'),
('550e8400-e29b-41d4-a716-446655440301', '11111111-0007-0007-0007-000000000001', 'x = 3', FALSE, '0'),
('550e8400-e29b-41d4-a716-446655440301', '11111111-0007-0007-0007-000000000002', 'x = 2', TRUE, '5');

-- =============================================
-- MOTIFS REJET
-- =============================================
-- Exercice non resolu pour demo eleve (QCM sans participation)
INSERT INTO ressources.exercises (id, nom, description, date_creation, etat, restriction, niveau, redacteur_id) VALUES
('ffffffff-0006-0006-0006-000000000006', 'QCM Histoire : La Revolution', 'Questions sur la Revolution francaise', '2025-03-01 08:00:00', 'PUBLIE', 'PUBLIC', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO ressources.exercise_matieres (exercise_id, matiere_id) VALUES
('ffffffff-0006-0006-0006-000000000006', 'aaaaaaaa-0001-0001-0001-000000000004');

INSERT INTO ressources.questions_reponses (id, intitule, reponse, type_question, exercise_id) VALUES
('11111111-0007-0007-0007-000000000010', 'En quelle annee a debute la Revolution francaise ?', '1789', 'QCM', 'ffffffff-0006-0006-0006-000000000006'),
('11111111-0007-0007-0007-000000000011', 'Qui etait roi de France en 1789 ?', 'Louis XVI', 'QCM', 'ffffffff-0006-0006-0006-000000000006'),
('11111111-0007-0007-0007-000000000012', 'Quel document a ete adopte en 1789 ?', 'La Declaration des droits de l homme', 'QCM', 'ffffffff-0006-0006-0006-000000000006');

INSERT INTO ressources.exercises_programmer (exercise_id, date_exo_prevue, date_debut_exo_effectif, date_fin_exo_effectif, etat_exercise_programmer, programme_par_id) VALUES
('ffffffff-0006-0006-0006-000000000006', '2025-04-10 09:00:00', '2025-04-10 09:00:00', '2025-04-10 10:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO ressources.exercise_programmer_classes (exercise_programmer_id, classe_id) VALUES
('ffffffff-0006-0006-0006-000000000006', '550e8400-e29b-41d4-a716-446655440407');

-- demo eleve inscrit mais n a pas encore repondu (pas de participer_exo ni repondre)

INSERT INTO ressources.motifs_rejet (id, code, descriptif, date_creation) VALUES
('mrej-001', 'DOCS_INVALIDES', 'Documents invalides ou illisibles', '2024-01-01 00:00:00'),
('mrej-002', 'PROFIL_INCOMPLET', 'Profil incomplet, informations manquantes', '2024-01-01 00:00:00'),
('mrej-003', 'DOUBLON', 'Compte deja existant avec cet email', '2024-01-01 00:00:00');

INSERT INTO ressources.motifs_rejet_classe (id, code, descriptif, date_creation) VALUES
('mrejc-001', 'NOM_INVALIDE', 'Nom de classe non conforme', '2024-01-01 00:00:00'),
('mrejc-002', 'ETABLISSEMENT_INCONNU', 'Etablissement non reconnu', '2024-01-01 00:00:00'),
('mrejc-003', 'DOUBLON_CLASSE', 'Classe deja existante dans cet etablissement', '2024-01-01 00:00:00');

-- =============================================
-- MEDIA (evenement_id -> evenements UUID FK)
-- =============================================
INSERT INTO ressources.media (id, bucket_name, content_type, file_name, file_path, file_size, file_type, media_type, owner_id, uploaded_date, evenement_id) VALUES
('med-001', 'scholchat', 'image/jpeg', 'photo_evt1.jpg', 'events/evt1/photo.jpg', 204800, 'IMAGE', 'PHOTO', '550e8400-e29b-41d4-a716-446655440007', '2025-01-15 17:30:00', 'cccccccc-0003-0003-0003-000000000001'),
('med-002', 'scholchat', 'application/pdf', 'programme.pdf', 'events/evt2/programme.pdf', 512000, 'DOCUMENT', 'TEXTE', '550e8400-e29b-41d4-a716-446655440008', '2025-02-01 10:00:00', 'cccccccc-0003-0003-0003-000000000002'),
('med-003', 'scholchat', 'image/png', 'schema_cours.png', 'cours/crs1/schema.png', 102400, 'IMAGE', 'PHOTO', '550e8400-e29b-41d4-a716-446655440007', '2024-12-01 09:00:00', NULL);

-- =============================================
-- INTERACTIONS (event_id -> evenements, message_id -> messages)
-- =============================================
INSERT INTO ressources.interactions (id, type, content, creation_date, niveau, created_by, event_id, message_id) VALUES
('int-001', 'COMMENT', 'Tres bon cours, merci !', '2024-12-05 10:00:00', 'CLASSE', '550e8400-e29b-41d4-a716-446655440300', 'cccccccc-0003-0003-0003-000000000003', NULL),
('int-002', 'LIKE', 'Contenu tres utile', '2024-12-05 11:00:00', 'CLASSE', '550e8400-e29b-41d4-a716-446655440301', 'cccccccc-0003-0003-0003-000000000003', NULL),
('int-003', 'COMMENT', 'Quand est le prochain cours ?', '2024-12-06 08:00:00', 'CLASSE', '550e8400-e29b-41d4-a716-446655440300', NULL, 'msg-001');

-- =============================================
-- DEMANDES ACCES
-- =============================================
INSERT INTO ressources.demandes_acces (id, utilisateur_id, classe_id, code_activation, etat, date_demande, est_parent, eleve_associe_id) VALUES
('da-001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440407', '111111', 'APPROUVEE', '2024-12-02 08:00:00', FALSE, NULL),
('da-002', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440407', '111111', 'EN_ATTENTE', '2024-12-03 09:00:00', FALSE, NULL),
('da-003', '550e8400-e29b-41d4-a716-446655440200', '550e8400-e29b-41d4-a716-446655440408', '222222', 'APPROUVEE', '2024-12-02 10:00:00', TRUE, '550e8400-e29b-41d4-a716-446655440300'),
('da-004', '550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440401', '234567', 'REJETEE', '2024-12-04 08:00:00', FALSE, NULL);

-- =============================================
-- HISTO ACTIVATION
-- =============================================
INSERT INTO ressources.histo_activation (id, classe_id, utilisateur_id, date_activation, is_active, etat_classe) VALUES
('aaaabbbb-0008-0008-0008-000000000001', '550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440007', '2024-11-28 08:00:00', TRUE, 'ACTIF'),
('aaaabbbb-0008-0008-0008-000000000002', '550e8400-e29b-41d4-a716-446655440404', '550e8400-e29b-41d4-a716-446655440007', '2024-11-30 08:00:00', FALSE, 'INACTIF'),
('aaaabbbb-0008-0008-0008-000000000003', '550e8400-e29b-41d4-a716-446655440407', '550e8400-e29b-41d4-a716-446655440007', '2024-12-01 11:00:00', TRUE, 'ACTIF');

-- =============================================
-- DEMO ELEVE for kpgpa237@gmail.com classes
-- =============================================
INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES
('demo-eleve-0000-0000-000000000001', 'Demo', 'Eleve', 'simokylian1@gmail.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0600000001', '1 Rue Demo', 'demo-eleve-token-001', 'ACTIVE', FALSE);

INSERT INTO ressources.eleves (eleves_id, niveau) VALUES
('demo-eleve-0000-0000-000000000001', '6eme');

-- Demo parent (parent du demo eleve)
INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES
('demo-parent-0000-0000-000000000001', 'Demo', 'Parent', 'parent.demo@scholchat.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0600000002', '1 Rue Demo', 'demo-parent-token-001', 'ACTIVE', FALSE);

INSERT INTO ressources.parents (parents_id) VALUES
('demo-parent-0000-0000-000000000001');

INSERT INTO ressources.parent_eleve (parent_id, eleve_id) VALUES
('demo-parent-0000-0000-000000000001', 'demo-eleve-0000-0000-000000000001');

-- Demo eleve in both active classes of kpgpa237
INSERT INTO ressources.classe_eleves (classe_id, eleve_id) VALUES
('550e8400-e29b-41d4-a716-446655440407', 'demo-eleve-0000-0000-000000000001'),
('550e8400-e29b-41d4-a716-446655440408', 'demo-eleve-0000-0000-000000000001');

-- Demo eleve access to both classes
INSERT INTO ressources.acceder (utilisateur_id, classe_id, date_acces) VALUES
('demo-eleve-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440407', '2024-12-02 08:00:00'),
('demo-eleve-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440408', '2024-12-02 08:00:00');

-- Demo eleve receives messages
INSERT INTO ressources.recevoir (message_id, utilisateur_id) VALUES
('msg-001', 'demo-eleve-0000-0000-000000000001'),
('msg-003', 'demo-eleve-0000-0000-000000000001');

-- Demo eleve participates in scheduled cours
INSERT INTO ressources.cours_programmer_participants (cours_programmer_id, utilisateur_id) VALUES
('cp-001', 'demo-eleve-0000-0000-000000000001'),
('cp-002', 'demo-eleve-0000-0000-000000000001');

-- Demo eleve participates in scheduled exercise
INSERT INTO ressources.participer_exo (utilisateur_id, exercise_programmer_id, note, appreciation, date_debut, date_fin) VALUES
('demo-eleve-0000-0000-000000000001', 'ffffffff-0006-0006-0006-000000000001', '18', 'Excellent', '2025-01-15 08:02:00', '2025-01-15 08:45:00');

-- Demo eleve answers
INSERT INTO ressources.repondre (utilisateur_id, question_id, reponse_utilisateur, est_correcte, note) VALUES
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000001', 'x = 2', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000002', 'x = 2', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000003', 'La deuxieme loi de Newton', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000004', 'La premiere loi de Newton', TRUE, '5');

-- Demo eleve participates in events
INSERT INTO ressources.evenement_participants (evenement_id, utilisateur_id) VALUES
('cccccccc-0003-0003-0003-000000000001', 'demo-eleve-0000-0000-000000000001'),
('cccccccc-0003-0003-0003-000000000003', 'demo-eleve-0000-0000-000000000001');

-- Progression chapitres du demo eleve
-- Cours equations (tous chapitres completes)
INSERT INTO ressources.progression_chapitre (utilisateur_id, chapitre_id, date_completion) VALUES
('demo-eleve-0000-0000-000000000001', 'eeeeeeee-0005-0005-0005-000000000001', '2025-01-10 09:00:00'),
('demo-eleve-0000-0000-000000000001', 'eeeeeeee-0005-0005-0005-000000000002', '2025-01-10 10:00:00'),
-- Cours forces (premier chapitre complete, deuxieme non)
('demo-eleve-0000-0000-000000000001', 'eeeeeeee-0005-0005-0005-000000000003', '2025-01-12 11:00:00');
-- Cours trigo et stats : aucun chapitre complete (exercice non resolu)

-- Statuts messages demo eleve
INSERT INTO ressources.message_statut (utilisateur_id, message_id, lu, favori, date_lecture) VALUES
('demo-eleve-0000-0000-000000000001', 'msg-001', true, true, '2025-01-05 09:00:00'),
('demo-eleve-0000-0000-000000000001', 'msg-002', true, false, '2025-01-06 10:00:00'),
('demo-eleve-0000-0000-000000000001', 'msg-003', false, false, NULL);

-- Demo eleve interactions
INSERT INTO ressources.interactions (id, type, content, creation_date, niveau, created_by, event_id, message_id) VALUES
('int-004', 'COMMENT', 'Super exercice, bien compris !', '2025-01-15 09:00:00', 'CLASSE', 'demo-eleve-0000-0000-000000000001', 'cccccccc-0003-0003-0003-000000000003', NULL),
('int-005', 'LIKE', 'Tres bon cours', '2025-01-10 10:00:00', 'CLASSE', 'demo-eleve-0000-0000-000000000001', NULL, 'msg-001');

-- =============================================
-- PRIVATE COURS owned by kpgpa237 (restriction PRIVE)
-- =============================================
INSERT INTO ressources.cours (id, titre, description, date_creation, etat, contenu, redacteur_id) VALUES
('dddddddd-0004-0004-0004-000000000005', 'Cours prive : Trigonometrie', 'Introduction a la trigonometrie - acces restreint', '2025-01-01 08:00:00', 'PUBLIE', 'La trigonometrie etudie les relations entre angles et cotes.', '550e8400-e29b-41d4-a716-446655440007'),
('dddddddd-0004-0004-0004-000000000006', 'Cours public : Statistiques', 'Introduction aux statistiques - acces libre', '2025-01-02 08:00:00', 'PUBLIE', 'Les statistiques permettent d analyser des donnees.', '550e8400-e29b-41d4-a716-446655440007');

-- Set restriction via UPDATE since schema adds the column via ALTER TABLE
UPDATE ressources.cours SET restriction = 'PRIVE' WHERE id = 'dddddddd-0004-0004-0004-000000000005';
UPDATE ressources.cours SET restriction = 'PUBLIC' WHERE id = 'dddddddd-0004-0004-0004-000000000006';

INSERT INTO ressources.cours_matiere (cours_id, matiere_id, ordre_dans_cours) VALUES
('dddddddd-0004-0004-0004-000000000005', 'aaaaaaaa-0001-0001-0001-000000000001', 2),
('dddddddd-0004-0004-0004-000000000006', 'aaaaaaaa-0001-0001-0001-000000000001', 3);

INSERT INTO ressources.chapitres (id, titre, description, ordre, contenu, cours_id) VALUES
('eeeeeeee-0005-0005-0005-000000000006', 'Sinus et cosinus', 'Definitions de base', 1, 'Le sinus est le rapport du cote oppose sur l hypotenuse.', 'dddddddd-0004-0004-0004-000000000005'),
('eeeeeeee-0005-0005-0005-000000000007', 'Moyenne et mediane', 'Indicateurs statistiques', 1, 'La moyenne est la somme divisee par le nombre de valeurs.', 'dddddddd-0004-0004-0004-000000000006');

-- Scheduled cours for private and public cours in Demo Class 1
INSERT INTO ressources.cours_programmer (id, cours_id, date_cours_prevue, etat_cours_programme, classe_id, lieu, description, date_creation, professeur_id) VALUES
('cp-004', 'dddddddd-0004-0004-0004-000000000005', '2025-02-05 08:00:00', 'PLANIFIE', '550e8400-e29b-41d4-a716-446655440407', 'Salle 101', 'Seance trigonometrie privee', '2025-01-20 08:00:00', '550e8400-e29b-41d4-a716-446655440007'),
('cp-005', 'dddddddd-0004-0004-0004-000000000006', '2025-02-07 08:00:00', 'PLANIFIE', '550e8400-e29b-41d4-a716-446655440407', 'Salle 101', 'Seance statistiques publique', '2025-01-20 09:00:00', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO ressources.cours_programmer_classes (cours_programmer_id, classe_id) VALUES
('cp-004', '550e8400-e29b-41d4-a716-446655440407'),
('cp-005', '550e8400-e29b-41d4-a716-446655440407');

INSERT INTO ressources.cours_programmer_participants (cours_programmer_id, utilisateur_id) VALUES
('cp-004', 'demo-eleve-0000-0000-000000000001'),
('cp-004', '550e8400-e29b-41d4-a716-446655440300'),
('cp-005', 'demo-eleve-0000-0000-000000000001'),
('cp-005', '550e8400-e29b-41d4-a716-446655440300');

-- =============================================
-- PRIVATE EXERCISE owned by kpgpa237 (restriction PRIVE)
-- =============================================
INSERT INTO ressources.exercises (id, nom, description, date_creation, etat, restriction, niveau, redacteur_id) VALUES
('ffffffff-0006-0006-0006-000000000004', 'Exercice prive : Trigo', 'Exercice de trigonometrie - acces restreint', '2025-01-05 08:00:00', 'PUBLIE', 'PRIVE', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440007'),
('ffffffff-0006-0006-0006-000000000005', 'Exercice public : Stats', 'Exercice de statistiques - acces libre', '2025-01-06 08:00:00', 'PUBLIE', 'PUBLIC', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO ressources.exercise_matieres (exercise_id, matiere_id) VALUES
('ffffffff-0006-0006-0006-000000000004', 'aaaaaaaa-0001-0001-0001-000000000001'),
('ffffffff-0006-0006-0006-000000000005', 'aaaaaaaa-0001-0001-0001-000000000001');

INSERT INTO ressources.cours_exercises (exercise_id, cours_id) VALUES
('ffffffff-0006-0006-0006-000000000004', 'dddddddd-0004-0004-0004-000000000005'),
('ffffffff-0006-0006-0006-000000000005', 'dddddddd-0004-0004-0004-000000000006');

INSERT INTO ressources.questions_reponses (id, intitule, reponse, type_question, exercise_id) VALUES
('11111111-0007-0007-0007-000000000006', 'Calculer sin(30)', '0.5', 'REPONSE_COURTE', 'ffffffff-0006-0006-0006-000000000004'),
('11111111-0007-0007-0007-000000000007', 'Calculer cos(0)', '1', 'REPONSE_COURTE', 'ffffffff-0006-0006-0006-000000000004'),
('11111111-0007-0007-0007-000000000008', 'Quelle est la moyenne de 2, 4, 6 ?', '4', 'REPONSE_COURTE', 'ffffffff-0006-0006-0006-000000000005'),
('11111111-0007-0007-0007-000000000009', 'Quelle est la mediane de 1, 3, 5 ?', '3', 'QCM', 'ffffffff-0006-0006-0006-000000000005');

-- Schedule private and public exercises in Demo Class 1
INSERT INTO ressources.exercises_programmer (exercise_id, date_exo_prevue, date_debut_exo_effectif, date_fin_exo_effectif, etat_exercise_programmer, programme_par_id) VALUES
('ffffffff-0006-0006-0006-000000000004', '2025-02-05 09:00:00', '2025-02-05 09:00:00', '2025-02-05 10:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007'),
('ffffffff-0006-0006-0006-000000000005', '2025-02-07 09:00:00', '2025-02-07 09:00:00', '2025-02-07 10:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO ressources.exercise_programmer_classes (exercise_programmer_id, classe_id) VALUES
('ffffffff-0006-0006-0006-000000000004', '550e8400-e29b-41d4-a716-446655440407'),
('ffffffff-0006-0006-0006-000000000005', '550e8400-e29b-41d4-a716-446655440407');

-- Demo eleve participates in new scheduled exercises
INSERT INTO ressources.participer_exo (utilisateur_id, exercise_programmer_id, note, appreciation, date_debut, date_fin) VALUES
('demo-eleve-0000-0000-000000000001', 'ffffffff-0006-0006-0006-000000000004', '16', 'Bien', '2025-02-05 09:05:00', '2025-02-05 09:50:00'),
('demo-eleve-0000-0000-000000000001', 'ffffffff-0006-0006-0006-000000000005', '19', 'Excellent', '2025-02-07 09:05:00', '2025-02-07 09:45:00');

-- Demo eleve answers for new exercises
INSERT INTO ressources.repondre (utilisateur_id, question_id, reponse_utilisateur, est_correcte, note) VALUES
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000006', '0.5', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000007', '1', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000008', '4', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000009', '3', TRUE, '5');

-- =============================================
-- EVENEMENT owned by kpgpa237 with demo eleve
-- =============================================
INSERT INTO ressources.evenements (id, titre, description, lieu, etat, heure_debut, heure_fin, createur_id) VALUES
('cccccccc-0003-0003-0003-000000000004', 'Seance revision trigo', 'Revision avant examen de trigonometrie', 'Salle 101', 'PLANIFIE', '2025-02-04 14:00:00', '2025-02-04 16:00:00', '550e8400-e29b-41d4-a716-446655440007'),
('cccccccc-0003-0003-0003-000000000005', 'Examen blanc statistiques', 'Simulation examen statistiques', 'Salle 101', 'PLANIFIE', '2025-02-06 08:00:00', '2025-02-06 10:00:00', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO ressources.evenement_participants (evenement_id, utilisateur_id) VALUES
('cccccccc-0003-0003-0003-000000000004', 'demo-eleve-0000-0000-000000000001'),
('cccccccc-0003-0003-0003-000000000004', '550e8400-e29b-41d4-a716-446655440300'),
('cccccccc-0003-0003-0003-000000000004', '550e8400-e29b-41d4-a716-446655440007'),
('cccccccc-0003-0003-0003-000000000005', 'demo-eleve-0000-0000-000000000001'),
('cccccccc-0003-0003-0003-000000000005', '550e8400-e29b-41d4-a716-446655440300'),
('cccccccc-0003-0003-0003-000000000005', '550e8400-e29b-41d4-a716-446655440007');

-- =============================================
-- NOTIFICATIONS for demo eleve
-- =============================================
INSERT INTO ressources.histo_activation (id, classe_id, utilisateur_id, date_activation, is_active, etat_classe) VALUES
('aaaabbbb-0008-0008-0008-000000000004', '550e8400-e29b-41d4-a716-446655440408', '550e8400-e29b-41d4-a716-446655440007', '2024-12-01 12:00:00', TRUE, 'ACTIF');

-- Demande acces for demo eleve (already approved via classe_eleves, this shows the flow)
INSERT INTO ressources.demandes_acces (id, utilisateur_id, classe_id, code_activation, etat, date_demande, date_traitement, est_parent, eleve_associe_id) VALUES
('da-005', 'demo-eleve-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440407', '111111', 'APPROUVEE', '2024-12-01 07:00:00', '2024-12-01 08:00:00', FALSE, NULL),
('da-006', 'demo-eleve-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440408', '222222', 'APPROUVEE', '2024-12-01 07:00:00', '2024-12-01 08:00:00', FALSE, NULL);

