SET search_path TO ressources;

INSERT INTO utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES

('550e8400-e29b-41d4-a716-446655440999', 'Admin', 'Super', 'admin@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456777', 'Admin Office', 'pepe', 'ACTIVE', TRUE),

('550e8400-e29b-41d4-a716-446655440000', 'Dupont', 'Jean', 'kemogneprince05@gmail.com', 'password123', '0123456789', '123 Rue de Paris', 'abc123activationcode1', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440001', 'Martin', 'Marie', 'marie.martin@example.com', 'password123', '0123456788', '456 Avenue de Lyon', 'abc123activationcode2', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440002', 'Durand', 'Pierre', 'pierre.durand@example.com', 'password123', '0123456787', '789 Boulevard de Nice', 'abc123activationcode3', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440003', 'Lefevre', 'Sophie', 'sophie.lefevre@example.com', 'password123', '0123456786', '101 Rue de Marseille', 'abc123activationcode4', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440004', 'Durand', 'Paul', 'peroldkamsu83@gmail.com', 'password123', '0123456785', '111 Rue de Lille', 'abc123activationcode5', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440007', 'Marie', 'Dupont', 'kpgpa237@gmail.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456789', '123 Rue de Paris', 'abc123activationcode6', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440008', 'Lucas', 'Martin', 'peroldkamsu33@gmail.com', 'password123', '0123456788', '456 Avenue de Lyon', 'abc123activationcode7', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440009', 'Isabelle', 'Lefevre', 'isabelle.lefevre@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456787', '789 Boulevard de Nice', 'abc123activationcode8', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440010', 'Paul', 'Durand', 'paul.durand@example.com', 'password123', '0123456786', '111 Rue de Lille', 'abc123activationcode9', 'ACTIVE', FALSE),
('gest-guy-001', 'Kamga', 'Guy', 'guy@scholchat.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '+237670000001', 'Douala, Cameroun', 'token-guy-001', 'ACTIVE', FALSE),
('gest-prince-001', 'Nkengfack', 'Prince', 'prince@scholchat.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '+237670000002', 'Yaoundé, Cameroun', 'token-prince-001', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440200', 'Parent A', 'FirstNameA', 'peroldkamsu38@gmail.com', 'password123', '0123456780', 'AddressA', 'abc123activationcode10', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440201', 'Parent B', 'FirstNameB', 'parentb@example.com', 'password123', '0123456781', 'AddressB', 'abc123activationcode11', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440300', 'Eleve A', 'Jean', 'jean.elevea@example.com', 'password123', '0123456700', '10 Rue des Écoles', 'abc123activationcode12', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440301', 'Eleve B', 'Marie', 'marie.eleveb@example.com', 'password123', '0123456701', '20 Rue des Lycées', 'abc123activationcode13', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440302', 'Eleve C', 'Paul', 'paul.elevec@example.com', 'password123', '0123456702', '30 Boulevard Université', 'abc123activationcode14', 'ACTIVE', FALSE),
('660e8400-e29b-41d4-a716-446655440999', 'Test', 'Professor', 'ulrich@gmail.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456789', 'Test Address', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluQGV4YW1wbGUuY29tIiwiaWF0IjoxNzQ0OTY3MzM1LCJleHAiOjE3NDQ5NjgyMzV9.NVeY4KP8KAM2Nh80NaFXYEJ4__ceTFOPQPe_pGryMQw', 'AWAITING_VALIDATION', FALSE);

INSERT INTO etablissements (id, nom, localisation, pays, email, telephone, option_envoi_mail_new_classe, option_token_general, code_unique, gestionnaire_id) VALUES
('550e8400-e29b-41d4-a716-446655440100', 'École Email Approval', 'Yaoundé', 'Cameroun', 'contact@etab-a.cm', '23712345678', TRUE, FALSE, 'ETB-12345678', '550e8400-e29b-41d4-a716-446655440999'),
('550e8400-e29b-41d4-a716-446655440101', 'Lycée Token General', 'Douala', 'Cameroun', 'info@etab-b.cm', '23787654321', TRUE, TRUE, 'ETB-87654321', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440102', 'Collège Code Unique', 'Bafoussam', 'Cameroun', 'admin@college-c.cm', '23798765432', FALSE, TRUE, 'ETB-11223344', '550e8400-e29b-41d4-a716-446655440007');


INSERT INTO professeurs (professeurs_id, cni_url_front, cni_url_back, selfie_url, matricule_professeur, has_uploaded) VALUES
('550e8400-e29b-41d4-a716-446655440007', 'https://example.com/cni/marie_dupont_front.jpg', 'https://example.com/cni/marie_dupont_back.jpg', 'https://example.com/selfie/marie_dupont_selfie.jpg', 'PROF-2024-001', true),
('550e8400-e29b-41d4-a716-446655440008', 'https://example.com/cni/lucas_martin_front.jpg', 'https://example.com/cni/lucas_martin_back.jpg', NULL, 'PROF-2024-002', true),
('550e8400-e29b-41d4-a716-446655440009', 'https://example.com/cni/isabelle_lefevre_front.jpg', 'https://example.com/cni/isabelle_lefevre_back.jpg', 'https://example.com/selfie/isabelle_lefevre_selfie.jpg', NULL, true),
('660e8400-e29b-41d4-a716-446655440999', 'http://example.com/cni.jpg', 'http://example.com/cni-back.jpg', 'http://example.com/selfie.jpg', 'PROF-TEST-001', true);

INSERT INTO parents (parents_id) VALUES
('550e8400-e29b-41d4-a716-446655440200'),
('550e8400-e29b-41d4-a716-446655440201');

INSERT INTO eleves (eleves_id, niveau) VALUES
('550e8400-e29b-41d4-a716-446655440002', 'Terminale'),
('550e8400-e29b-41d4-a716-446655440003', 'Terminale'),
('550e8400-e29b-41d4-a716-446655440300', '6eme'),
('550e8400-e29b-41d4-a716-446655440301', '5eme'),
('550e8400-e29b-41d4-a716-446655440302', '3eme');


INSERT INTO repetiteurs (repetiteurs_id, cni_url_front, cni_url_back, photo_full_picture, nom_classe) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'https://example.com/cni/paul_durand_front.jpg', 'https://example.com/cni/paul_durand_back.jpg', 'https://example.com/photos/paul_durand_full.jpg', 'Terminale B');

INSERT INTO gestionnaires (gestionnaires_id) VALUES
('gest-guy-001'),
('gest-prince-001');


INSERT INTO classes (id, nom, niveau, date_creation, code_activation, etat, etablissement_id, moderator_id, acces_majeur, payment_required) VALUES
('550e8400-e29b-41d4-a716-446655440400', 'Classe A', '3ème', '2024-11-28 08:00:00', '123456', 'ACTIF', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440007', FALSE, FALSE),
('550e8400-e29b-41d4-a716-446655440401', 'Classe B', '2nde', '2024-11-28 09:00:00', '234567', 'ACTIF', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440008', TRUE, FALSE),
('550e8400-e29b-41d4-a716-446655440402', 'Classe C - Pending', '4ème', '2024-11-29 09:00:00', '789012', 'EN_ATTENTE_APPROBATION', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440009', FALSE, FALSE),
('550e8400-e29b-41d4-a716-446655440403', 'Classe D - Pending', '1ère', '2024-11-29 10:00:00', '890123', 'EN_ATTENTE_APPROBATION', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440008', TRUE, FALSE),
('550e8400-e29b-41d4-a716-446655440404', 'Classe E - Inactive', '4ème', '2024-11-30 08:00:00', '901234', 'INACTIF', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440007', FALSE, FALSE),
('550e8400-e29b-41d4-a716-446655440405', 'Classe F - Inactive', 'Terminale', '2024-11-30 09:00:00', '012345', 'INACTIF', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440008', FALSE, FALSE),
('550e8400-e29b-41d4-a716-446655440406', 'Independent Class', 'CE1', '2024-12-01 10:00:00', '567890', 'EN_ATTENTE_APPROBATION', NULL, NULL, FALSE, TRUE),

('550e8400-e29b-41d4-a716-446655440407', 'Demo Class 1', '6ème', '2024-12-01 11:00:00', '111111', 'ACTIF', '550e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440007', TRUE, FALSE),
('550e8400-e29b-41d4-a716-446655440408', 'Demo Class 2', '5ème', '2024-12-01 12:00:00', '222222', 'ACTIF', '550e8400-e29b-41d4-a716-446655440102', '550e8400-e29b-41d4-a716-446655440007', TRUE, FALSE);

INSERT INTO classe_parents (classe_id, parent_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440200'),
('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440201'),

('550e8400-e29b-41d4-a716-446655440407', '550e8400-e29b-41d4-a716-446655440200'),
('550e8400-e29b-41d4-a716-446655440408', '550e8400-e29b-41d4-a716-446655440201');

INSERT INTO classe_eleves (classe_id, eleve_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440300'),
('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440301'),
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440302'),

('550e8400-e29b-41d4-a716-446655440407', '550e8400-e29b-41d4-a716-446655440300'),
('550e8400-e29b-41d4-a716-446655440408', '550e8400-e29b-41d4-a716-446655440301');

INSERT INTO professeur_classes_moderees (professeur_id, classe_id) VALUES
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440400'),
('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440401'),
('550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440402'),
('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440403'),
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440404'),
('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440405'),

('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440407'),
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440408');


-- Default matieres (subjects)
-- IDs canoniques utilises partout dans ce fichier :
--   Mathematiques  -> aaaaaaaa-0001-0001-0001-000000000001
--   Physique-Chimie-> aaaaaaaa-0001-0001-0001-000000000002
--   Francais       -> aaaaaaaa-0001-0001-0001-000000000003
--   Histoire-Geo   -> aaaaaaaa-0001-0001-0001-000000000004
--   SVT            -> aaaaaaaa-0001-0001-0001-000000000005
INSERT INTO matieres (id, nom, description, date_creation, etat) VALUES
('aaaaaaaa-0001-0001-0001-000000000001', 'Mathematiques', 'Algebre, geometrie, arithmetique, analyse', NOW(), 'ACTIF'),
('aaaaaaaa-0001-0001-0001-000000000003', 'Francais', 'Grammaire, conjugaison, orthographe, litterature', NOW(), 'ACTIF'),
('mat-angl-001', 'Anglais', 'English language and literature', NOW(), 'ACTIF'),
('mat-phys-001', 'Physique', 'Mecanique, optique, electricite, thermodynamique', NOW(), 'ACTIF'),
('mat-chim-001', 'Chimie', 'Chimie organique, inorganique, solutions', NOW(), 'ACTIF'),
('aaaaaaaa-0001-0001-0001-000000000005', 'SVT', 'Sciences de la Vie et de la Terre, biologie, geologie', NOW(), 'ACTIF'),
('aaaaaaaa-0001-0001-0001-000000000004', 'Histoire-Geographie', 'Histoire et geographie', NOW(), 'ACTIF'),
('mat-geo-001', 'Geographie', 'Geographie physique et humaine, cartographie', NOW(), 'ACTIF'),
('mat-phil-001', 'Philosophie', 'Logique, ethique, epistemologie, metaphysique', NOW(), 'ACTIF'),
('mat-info-001', 'Informatique', 'Algorithmique, programmation, systemes', NOW(), 'ACTIF'),
('mat-edc-001', 'Education Civique', 'Citoyennete, droits et devoirs', NOW(), 'ACTIF'),
('mat-eps-001', 'EPS', 'Education Physique et Sportive', NOW(), 'ACTIF'),
('mat-art-001', 'Arts Plastiques', 'Dessin, peinture, sculpture', NOW(), 'ACTIF'),
('mat-mus-001', 'Musique', 'Theorie musicale, pratique instrumentale et vocale', NOW(), 'ACTIF'),
('mat-esp-001', 'Espagnol', 'Langue et culture espagnole', NOW(), 'ACTIF'),
('mat-all-001', 'Allemand', 'Langue et culture allemande', NOW(), 'ACTIF'),
('mat-eco-001', 'Economie', 'Microeconomie, macroeconomie, comptabilite', NOW(), 'ACTIF'),
('mat-drt-001', 'Droit', 'Droit civil, droit des affaires', NOW(), 'ACTIF'),
('aaaaaaaa-0001-0001-0001-000000000002', 'Physique-Chimie', 'Sciences physiques et chimiques', NOW(), 'ACTIF')
ON CONFLICT (id) DO NOTHING;

INSERT INTO droit_publication (utilisateur_id, classe_id, date_attribution, peut_publier, peut_moderer) VALUES
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00', TRUE, TRUE),
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440408', '2024-12-01 12:00:00', TRUE, TRUE);

INSERT INTO acceder (utilisateur_id, classe_id, date_acces) VALUES
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00'),
('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440408', '2024-12-01 12:00:00'),
('550e8400-e29b-41d4-a716-446655440300', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00'),
('550e8400-e29b-41d4-a716-446655440301', '550e8400-e29b-41d4-a716-446655440408', '2024-12-01 12:00:00'),
('550e8400-e29b-41d4-a716-446655440200', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00'),
('550e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440408', '2024-12-01 12:00:00'),
('550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00'),
('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440407', '2024-12-01 11:00:00');

-- Add all class moderators to acceder so they appear as class members
INSERT INTO acceder (utilisateur_id, classe_id)
SELECT moderator_id, id FROM classes WHERE moderator_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Give admins access to ALL classes
INSERT INTO acceder (utilisateur_id, classe_id)
SELECT u.id, c.id FROM utilisateurs u CROSS JOIN classes c WHERE u.is_admin = true
ON CONFLICT DO NOTHING;


-- =============================================
-- SYNCED FROM data-h2.sql
-- =============================================

-- matieres deja inserees ci-dessus avec les IDs canoniques aaaaaaaa-*

INSERT INTO classe_matieres (matiere_id, classe_id) VALUES
('aaaaaaaa-0001-0001-0001-000000000001', '550e8400-e29b-41d4-a716-446655440400'),
('aaaaaaaa-0001-0001-0001-000000000002', '550e8400-e29b-41d4-a716-446655440400'),
('aaaaaaaa-0001-0001-0001-000000000003', '550e8400-e29b-41d4-a716-446655440401'),
('aaaaaaaa-0001-0001-0001-000000000001', '550e8400-e29b-41d4-a716-446655440407'),
('aaaaaaaa-0001-0001-0001-000000000002', '550e8400-e29b-41d4-a716-446655440407'),
('aaaaaaaa-0001-0001-0001-000000000003', '550e8400-e29b-41d4-a716-446655440408'),
('aaaaaaaa-0001-0001-0001-000000000004', '550e8400-e29b-41d4-a716-446655440408');

INSERT INTO canaux (id, nom, description, professeur_id, classe_id) VALUES
('bbbbbbbb-0002-0002-0002-000000000001', 'General Classe A', 'Canal principal de la Classe A', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440400'),
('bbbbbbbb-0002-0002-0002-000000000002', 'Mathematiques Demo1', 'Canal maths Demo Class 1', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440407'),
('bbbbbbbb-0002-0002-0002-000000000003', 'Physique Demo1', 'Canal physique Demo Class 1', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440407'),
('bbbbbbbb-0002-0002-0002-000000000004', 'Francais Classe B', 'Canal francais Classe B', '550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440401');

INSERT INTO messages (id, objet, contenu, datecreation, etat, expediteur_id, deleted) VALUES
('msg-001', 'Bienvenue', 'Bienvenue dans la classe !', '2024-12-02 08:00:00', 'ENVOYE', '550e8400-e29b-41d4-a716-446655440007', FALSE),
('msg-002', 'Devoir', 'Le devoir est pour vendredi.', '2024-12-03 09:00:00', 'ENVOYE', '550e8400-e29b-41d4-a716-446655440008', FALSE),
('msg-003', 'Rappel', 'Rappel : reunion parents demain.', '2024-12-04 10:00:00', 'ENVOYE', '550e8400-e29b-41d4-a716-446655440007', FALSE);

INSERT INTO recevoir (message_id, utilisateur_id) VALUES
('msg-001', '550e8400-e29b-41d4-a716-446655440300'),
('msg-001', '550e8400-e29b-41d4-a716-446655440301'),
('msg-002', '550e8400-e29b-41d4-a716-446655440300'),
('msg-003', '550e8400-e29b-41d4-a716-446655440200');

INSERT INTO message_classes (message_id, classe_id) VALUES
('msg-001', '550e8400-e29b-41d4-a716-446655440407'),
('msg-002', '550e8400-e29b-41d4-a716-446655440401'),
('msg-003', '550e8400-e29b-41d4-a716-446655440407');

INSERT INTO evenements (id, titre, description, lieu, etat, heure_debut, heure_fin, createur_id, visibility) VALUES
('cccccccc-0003-0003-0003-000000000001', 'Reunion parents-professeurs', 'Reunion trimestrielle', 'Salle A', 'PLANIFIE', '2025-01-15 17:00:00', '2025-01-15 19:00:00', '550e8400-e29b-41d4-a716-446655440007', 'PRIVATE'),
('cccccccc-0003-0003-0003-000000000002', 'Sortie scolaire', 'Visite du musee national', 'Musee National', 'PLANIFIE', '2025-02-10 08:00:00', '2025-02-10 17:00:00', '550e8400-e29b-41d4-a716-446655440008', 'PUBLIC'),
('cccccccc-0003-0003-0003-000000000003', 'Concours de maths', 'Competition inter-classes', 'Amphi', 'PASSE', '2024-12-05 09:00:00', '2024-12-05 12:00:00', '550e8400-e29b-41d4-a716-446655440007', 'PRIVATE');

INSERT INTO evenement_participants (evenement_id, utilisateur_id) VALUES
('cccccccc-0003-0003-0003-000000000001', '550e8400-e29b-41d4-a716-446655440200'),
('cccccccc-0003-0003-0003-000000000001', '550e8400-e29b-41d4-a716-446655440201'),
('cccccccc-0003-0003-0003-000000000001', '550e8400-e29b-41d4-a716-446655440007'),
('cccccccc-0003-0003-0003-000000000002', '550e8400-e29b-41d4-a716-446655440300'),
('cccccccc-0003-0003-0003-000000000002', '550e8400-e29b-41d4-a716-446655440301'),
('cccccccc-0003-0003-0003-000000000003', '550e8400-e29b-41d4-a716-446655440300'),
('cccccccc-0003-0003-0003-000000000003', '550e8400-e29b-41d4-a716-446655440302');

-- Link events to classes
INSERT INTO evenement_classes (evenement_id, classe_id) VALUES
('cccccccc-0003-0003-0003-000000000001', '550e8400-e29b-41d4-a716-446655440407'),
('cccccccc-0003-0003-0003-000000000001', '550e8400-e29b-41d4-a716-446655440408'),
('cccccccc-0003-0003-0003-000000000003', '550e8400-e29b-41d4-a716-446655440407');

INSERT INTO cours (id, titre, description, date_creation, etat, contenu, redacteur_id) VALUES
('dddddddd-0004-0004-0004-000000000001', 'Introduction aux equations', 'Bases des equations du 1er degre', '2024-12-01 08:00:00', 'PUBLIE', 'Une equation est une egalite comportant une inconnue.', '550e8400-e29b-41d4-a716-446655440007'),
('dddddddd-0004-0004-0004-000000000002', 'Les forces en physique', 'Newton et les lois du mouvement', '2024-12-02 08:00:00', 'PUBLIE', 'Une force est une action mecanique exercee sur un corps.', '550e8400-e29b-41d4-a716-446655440007'),
('dddddddd-0004-0004-0004-000000000003', 'La dissertation francaise', 'Methodologie de la dissertation', '2024-12-03 08:00:00', 'BROUILLON', 'La dissertation est un exercice de reflexion structuree.', '550e8400-e29b-41d4-a716-446655440008'),
('dddddddd-0004-0004-0004-000000000004', 'La Revolution francaise', 'Causes et consequences', '2024-12-04 08:00:00', 'PUBLIE', 'La Revolution francaise debuta en 1789.', '550e8400-e29b-41d4-a716-446655440009');

INSERT INTO cours_matiere (cours_id, matiere_id, ordre_dans_cours) VALUES
('dddddddd-0004-0004-0004-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001', 1),
('dddddddd-0004-0004-0004-000000000002', 'aaaaaaaa-0001-0001-0001-000000000002', 1),
('dddddddd-0004-0004-0004-000000000003', 'aaaaaaaa-0001-0001-0001-000000000003', 1),
('dddddddd-0004-0004-0004-000000000004', 'aaaaaaaa-0001-0001-0001-000000000004', 1);

INSERT INTO chapitres (id, titre, description, ordre, contenu, cours_id) VALUES
('eeeeeeee-0005-0005-0005-000000000001', 'Definition et vocabulaire', 'Vocabulaire de base', 1, 'Une equation est composee de deux membres.', 'dddddddd-0004-0004-0004-000000000001'),
('eeeeeeee-0005-0005-0005-000000000002', 'Resolution graphique', 'Methode graphique', 2, 'On peut resoudre une equation graphiquement.', 'dddddddd-0004-0004-0004-000000000001'),
('eeeeeeee-0005-0005-0005-000000000003', 'Premiere loi de Newton', 'Principe inertie', 1, 'Tout corps persevere dans son etat de repos.', 'dddddddd-0004-0004-0004-000000000002'),
('eeeeeeee-0005-0005-0005-000000000004', 'Deuxieme loi de Newton', 'Principe fondamental', 2, 'La somme des forces est egale a m*a.', 'dddddddd-0004-0004-0004-000000000002'),
('eeeeeeee-0005-0005-0005-000000000005', 'Introduction et plan', 'Structure de la dissertation', 1, 'Une dissertation comporte trois parties.', 'dddddddd-0004-0004-0004-000000000003');

INSERT INTO exercises (id, nom, description, date_creation, etat, restriction, niveau, redacteur_id) VALUES
('ffffffff-0006-0006-0006-000000000001', 'Exercice equations 3eme', 'Resoudre des equations simples', '2024-12-05 08:00:00', 'PUBLIE', 'PUBLIC', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440007'),
('ffffffff-0006-0006-0006-000000000002', 'QCM Forces', 'Questions sur les forces', '2024-12-06 08:00:00', 'PUBLIE', 'PUBLIC', 'LYCEE', '550e8400-e29b-41d4-a716-446655440007'),
('ffffffff-0006-0006-0006-000000000003', 'Analyse de texte', 'Exercice de comprehension', '2024-12-07 08:00:00', 'PUBLIE', 'PRIVE', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440008');

INSERT INTO exercise_matieres (exercise_id, matiere_id) VALUES
('ffffffff-0006-0006-0006-000000000001', 'aaaaaaaa-0001-0001-0001-000000000001'),
('ffffffff-0006-0006-0006-000000000002', 'aaaaaaaa-0001-0001-0001-000000000002'),
('ffffffff-0006-0006-0006-000000000003', 'aaaaaaaa-0001-0001-0001-000000000003');

INSERT INTO cours_exercises (exercise_id, cours_id) VALUES
('ffffffff-0006-0006-0006-000000000001', 'dddddddd-0004-0004-0004-000000000001'),
('ffffffff-0006-0006-0006-000000000002', 'dddddddd-0004-0004-0004-000000000002'),
('ffffffff-0006-0006-0006-000000000003', 'dddddddd-0004-0004-0004-000000000003');

INSERT INTO questions_reponses (id, intitule, reponse, type_question, exercise_id) VALUES
('11111111-0007-0007-0007-000000000001', 'Resoudre : 2x + 3 = 7', 'x = 2', 'REPONSE_COURTE', 'ffffffff-0006-0006-0006-000000000001'),
('11111111-0007-0007-0007-000000000002', 'Resoudre : 5x - 10 = 0', 'x = 2', 'REPONSE_COURTE', 'ffffffff-0006-0006-0006-000000000001'),
('11111111-0007-0007-0007-000000000003', 'Quelle loi enonce que F = ma ?', 'La deuxieme loi de Newton', 'QCM', 'ffffffff-0006-0006-0006-000000000002'),
('11111111-0007-0007-0007-000000000004', 'L inertie est decrite par quelle loi ?', 'La premiere loi de Newton', 'QCM', 'ffffffff-0006-0006-0006-000000000002'),
('11111111-0007-0007-0007-000000000005', 'Quel est le theme principal du texte ?', NULL, 'REPONSE_LONGUE', 'ffffffff-0006-0006-0006-000000000003');

INSERT INTO cours_programmer (id, cours_id, date_cours_prevue, etat_cours_programme, classe_id, lieu, description, date_creation, professeur_id) VALUES
('cp-001', 'dddddddd-0004-0004-0004-000000000001', '2025-01-10 08:00:00', 'PLANIFIE', '550e8400-e29b-41d4-a716-446655440407', 'Salle 101', 'Seance 1 equations', '2024-12-10 08:00:00', '550e8400-e29b-41d4-a716-446655440007'),
('cp-002', 'dddddddd-0004-0004-0004-000000000002', '2025-01-12 10:00:00', 'PLANIFIE', '550e8400-e29b-41d4-a716-446655440407', 'Labo Physique', 'Seance forces', '2024-12-10 09:00:00', '550e8400-e29b-41d4-a716-446655440007'),
('cp-003', 'dddddddd-0004-0004-0004-000000000003', '2025-01-14 08:00:00', 'TERMINE', '550e8400-e29b-41d4-a716-446655440401', 'Salle 202', 'Seance dissertation', '2024-12-11 08:00:00', '550e8400-e29b-41d4-a716-446655440008');

INSERT INTO cours_programmer_classes (cours_programmer_id, classe_id) VALUES
('cp-001', '550e8400-e29b-41d4-a716-446655440407'),
('cp-002', '550e8400-e29b-41d4-a716-446655440407'),
('cp-003', '550e8400-e29b-41d4-a716-446655440401');

INSERT INTO cours_programmer_participants (cours_programmer_id, utilisateur_id) VALUES
('cp-001', '550e8400-e29b-41d4-a716-446655440300'),
('cp-001', '550e8400-e29b-41d4-a716-446655440301'),
('cp-002', '550e8400-e29b-41d4-a716-446655440300'),
('cp-003', '550e8400-e29b-41d4-a716-446655440301');

INSERT INTO exercises_programmer (id, source_exercise_id, type_assignation, date_exo_prevue, date_debut_exo_effectif, date_fin_exo_effectif, etat_exercise_programmer, programme_par_id) VALUES
('ffffffff-0006-0006-0006-000000000001', 'ffffffff-0006-0006-0006-000000000001', 'EXERCICE', '2025-01-15 08:00:00', '2025-01-15 08:00:00', '2025-01-15 09:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007'),
('ffffffff-0006-0006-0006-000000000002', 'ffffffff-0006-0006-0006-000000000002', 'EXERCICE', '2025-01-17 10:00:00', '2025-01-17 10:00:00', '2025-01-17 11:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO exercise_programmer_classes (exercise_programmer_id, classe_id) VALUES
('ffffffff-0006-0006-0006-000000000001', '550e8400-e29b-41d4-a716-446655440407'),
('ffffffff-0006-0006-0006-000000000002', '550e8400-e29b-41d4-a716-446655440407');

-- Exercice non resolu pour demo eleve (QCM sans participation)
INSERT INTO exercises (id, nom, description, date_creation, etat, restriction, niveau, redacteur_id) VALUES
('ffffffff-0006-0006-0006-000000000006', 'QCM Histoire : La Revolution', 'Questions sur la Revolution francaise', '2025-03-01 08:00:00', 'PUBLIE', 'PUBLIC', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO exercise_matieres (exercise_id, matiere_id) VALUES
('ffffffff-0006-0006-0006-000000000006', 'aaaaaaaa-0001-0001-0001-000000000004');

INSERT INTO questions_reponses (id, intitule, reponse, type_question, exercise_id) VALUES
('11111111-0007-0007-0007-000000000010', 'En quelle annee a debute la Revolution francaise ?', '1789', 'QCM', 'ffffffff-0006-0006-0006-000000000006'),
('11111111-0007-0007-0007-000000000011', 'Qui etait roi de France en 1789 ?', 'Louis XVI', 'QCM', 'ffffffff-0006-0006-0006-000000000006'),
('11111111-0007-0007-0007-000000000012', 'Quel document a ete adopte en 1789 ?', 'La Declaration des droits de l homme', 'QCM', 'ffffffff-0006-0006-0006-000000000006');

INSERT INTO exercises_programmer (id, source_exercise_id, type_assignation, date_exo_prevue, date_debut_exo_effectif, date_fin_exo_effectif, etat_exercise_programmer, programme_par_id) VALUES
('ffffffff-0006-0006-0006-000000000006', 'ffffffff-0006-0006-0006-000000000006', 'EXERCICE', '2025-04-10 09:00:00', '2025-04-10 09:00:00', '2025-04-10 10:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO exercise_programmer_classes (exercise_programmer_id, classe_id) VALUES
('ffffffff-0006-0006-0006-000000000006', '550e8400-e29b-41d4-a716-446655440407');

-- demo eleve inscrit mais n a pas encore repondu (pas de participer_exo ni repondre)

INSERT INTO motifs_rejet (id, code, descriptif, date_creation) VALUES
('mrej-001', 'DOCS_INVALIDES', 'Documents invalides ou illisibles', '2024-01-01 00:00:00'),
('mrej-002', 'PROFIL_INCOMPLET', 'Profil incomplet, informations manquantes', '2024-01-01 00:00:00'),
('mrej-003', 'DOUBLON', 'Compte deja existant avec cet email', '2024-01-01 00:00:00');

INSERT INTO motifs_rejet_classe (id, code, descriptif, date_creation) VALUES
('mrejc-001', 'NOM_INVALIDE', 'Nom de classe non conforme', '2024-01-01 00:00:00'),
('mrejc-002', 'ETABLISSEMENT_INCONNU', 'Etablissement non reconnu', '2024-01-01 00:00:00'),
('mrejc-003', 'DOUBLON_CLASSE', 'Classe deja existante dans cet etablissement', '2024-01-01 00:00:00');

INSERT INTO media (id, bucket_name, content_type, file_name, file_path, file_size, file_type, media_type, owner_id, uploaded_date, evenement_id) VALUES
('med-001', 'scholchat', 'image/jpeg', 'photo_evt1.jpg', 'events/evt1/photo.jpg', 204800, 'IMAGE', 'PHOTO', '550e8400-e29b-41d4-a716-446655440007', '2025-01-15 17:30:00', 'cccccccc-0003-0003-0003-000000000001'),
('med-002', 'scholchat', 'application/pdf', 'programme.pdf', 'events/evt2/programme.pdf', 512000, 'DOCUMENT', 'TEXTE', '550e8400-e29b-41d4-a716-446655440008', '2025-02-01 10:00:00', 'cccccccc-0003-0003-0003-000000000002'),
('med-003', 'scholchat', 'image/png', 'schema_cours.png', 'cours/crs1/schema.png', 102400, 'IMAGE', 'PHOTO', '550e8400-e29b-41d4-a716-446655440007', '2024-12-01 09:00:00', NULL);

INSERT INTO interactions (id, type, content, creation_date, niveau, created_by, event_id, message_id) VALUES
('int-001', 'COMMENT', 'Tres bon cours, merci !', '2024-12-05 10:00:00', 'CLASSE', '550e8400-e29b-41d4-a716-446655440300', 'cccccccc-0003-0003-0003-000000000003', NULL),
('int-002', 'LIKE', 'Contenu tres utile', '2024-12-05 11:00:00', 'CLASSE', '550e8400-e29b-41d4-a716-446655440301', 'cccccccc-0003-0003-0003-000000000003', NULL),
('int-003', 'COMMENT', 'Quand est le prochain cours ?', '2024-12-06 08:00:00', 'CLASSE', '550e8400-e29b-41d4-a716-446655440300', NULL, 'msg-001');

INSERT INTO demandes_acces (id, utilisateur_id, classe_id, code_activation, etat, date_demande, est_parent, eleve_associe_id) VALUES
('da-001', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440407', '111111', 'APPROUVEE', '2024-12-02 08:00:00', FALSE, NULL),
('da-002', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440407', '111111', 'EN_ATTENTE', '2024-12-03 09:00:00', FALSE, NULL),
('da-003', '550e8400-e29b-41d4-a716-446655440200', '550e8400-e29b-41d4-a716-446655440408', '222222', 'APPROUVEE', '2024-12-02 10:00:00', TRUE, '550e8400-e29b-41d4-a716-446655440300'),
('da-004', '550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440401', '234567', 'REJETEE', '2024-12-04 08:00:00', FALSE, NULL);

INSERT INTO histo_activation (id, classe_id, utilisateur_id, date_activation, is_active, etat_classe) VALUES
('aaaabbbb-0008-0008-0008-000000000001', '550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440007', '2024-11-28 08:00:00', TRUE, 'ACTIF'),
('aaaabbbb-0008-0008-0008-000000000002', '550e8400-e29b-41d4-a716-446655440404', '550e8400-e29b-41d4-a716-446655440007', '2024-11-30 08:00:00', FALSE, 'INACTIF'),
('aaaabbbb-0008-0008-0008-000000000003', '550e8400-e29b-41d4-a716-446655440407', '550e8400-e29b-41d4-a716-446655440007', '2024-12-01 11:00:00', TRUE, 'ACTIF');

INSERT INTO utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES
('demo-eleve-0000-0000-000000000001', 'Demo', 'Eleve', 'simokylian1@gmail.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0600000001', '1 Rue Demo', 'demo-eleve-token-001', 'ACTIVE', FALSE);

INSERT INTO eleves (eleves_id, niveau) VALUES
('demo-eleve-0000-0000-000000000001', '6eme');

-- Demo parent (parent du demo eleve)
INSERT INTO utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES
('demo-parent-0000-0000-000000000001', 'Demo', 'Parent', 'parent.demo@scholchat.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0600000002', '1 Rue Demo', 'demo-parent-token-001', 'ACTIVE', FALSE);

INSERT INTO parents (parents_id) VALUES
('demo-parent-0000-0000-000000000001');

INSERT INTO parent_eleve (parent_id, eleve_id) VALUES
('demo-parent-0000-0000-000000000001', 'demo-eleve-0000-0000-000000000001');

INSERT INTO acceder (utilisateur_id, classe_id, date_acces) VALUES
('demo-eleve-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440407', '2024-12-02 08:00:00'),
('demo-eleve-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440408', '2024-12-02 08:00:00');

INSERT INTO recevoir (message_id, utilisateur_id) VALUES
('msg-001', 'demo-eleve-0000-0000-000000000001'),
('msg-003', 'demo-eleve-0000-0000-000000000001');

INSERT INTO cours_programmer_participants (cours_programmer_id, utilisateur_id) VALUES
('cp-001', 'demo-eleve-0000-0000-000000000001'),
('cp-002', 'demo-eleve-0000-0000-000000000001');

INSERT INTO participer_exo (utilisateur_id, exercise_programmer_id, etat_soumission, note, appreciation, date_debut, date_fin) VALUES
('demo-eleve-0000-0000-000000000001', 'ffffffff-0006-0006-0006-000000000001', 'CORRIGE', '18', 'Excellent', '2025-01-15 08:02:00', '2025-01-15 08:45:00');

INSERT INTO repondre (utilisateur_id, question_id, reponse_utilisateur, est_correcte, note) VALUES
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000001', 'x = 2', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000002', 'x = 2', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000003', 'La deuxieme loi de Newton', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000004', 'La premiere loi de Newton', TRUE, '5');

INSERT INTO evenement_participants (evenement_id, utilisateur_id) VALUES
('cccccccc-0003-0003-0003-000000000001', 'demo-eleve-0000-0000-000000000001'),
('cccccccc-0003-0003-0003-000000000003', 'demo-eleve-0000-0000-000000000001');

-- Progression chapitres du demo eleve
-- Cours equations (tous chapitres completes)
INSERT INTO progression_chapitre (utilisateur_id, chapitre_id, date_completion) VALUES
('demo-eleve-0000-0000-000000000001', 'eeeeeeee-0005-0005-0005-000000000001', '2025-01-10 09:00:00'),
('demo-eleve-0000-0000-000000000001', 'eeeeeeee-0005-0005-0005-000000000002', '2025-01-10 10:00:00'),
-- Cours forces (premier chapitre complete, deuxieme non)
('demo-eleve-0000-0000-000000000001', 'eeeeeeee-0005-0005-0005-000000000003', '2025-01-12 11:00:00');
-- Cours trigo et stats : aucun chapitre complete (exercice non resolu)

-- Statuts messages demo eleve
INSERT INTO message_statut (utilisateur_id, message_id, lu, favori, date_lecture) VALUES
('demo-eleve-0000-0000-000000000001', 'msg-001', true, true, '2025-01-05 09:00:00'),
('demo-eleve-0000-0000-000000000001', 'msg-002', true, false, '2025-01-06 10:00:00'),
('demo-eleve-0000-0000-000000000001', 'msg-003', false, false, NULL);

INSERT INTO interactions (id, type, content, creation_date, niveau, created_by, event_id, message_id) VALUES
('int-004', 'COMMENT', 'Super exercice, bien compris !', '2025-01-15 09:00:00', 'CLASSE', 'demo-eleve-0000-0000-000000000001', 'cccccccc-0003-0003-0003-000000000003', NULL),
('int-005', 'LIKE', 'Tres bon cours', '2025-01-10 10:00:00', 'CLASSE', 'demo-eleve-0000-0000-000000000001', NULL, 'msg-001');

INSERT INTO cours (id, titre, description, date_creation, etat, contenu, redacteur_id) VALUES
('dddddddd-0004-0004-0004-000000000005', 'Cours prive : Trigonometrie', 'Introduction a la trigonometrie - acces restreint', '2025-01-01 08:00:00', 'PUBLIE', 'La trigonometrie etudie les relations entre angles et cotes.', '550e8400-e29b-41d4-a716-446655440007'),
('dddddddd-0004-0004-0004-000000000006', 'Cours public : Statistiques', 'Introduction aux statistiques - acces libre', '2025-01-02 08:00:00', 'PUBLIE', 'Les statistiques permettent d analyser des donnees.', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO cours_matiere (cours_id, matiere_id, ordre_dans_cours) VALUES
('dddddddd-0004-0004-0004-000000000005', 'aaaaaaaa-0001-0001-0001-000000000001', 2),
('dddddddd-0004-0004-0004-000000000006', 'aaaaaaaa-0001-0001-0001-000000000001', 3);

INSERT INTO chapitres (id, titre, description, ordre, contenu, cours_id) VALUES
('eeeeeeee-0005-0005-0005-000000000006', 'Sinus et cosinus', 'Definitions de base', 1, 'Le sinus est le rapport du cote oppose sur l hypotenuse.', 'dddddddd-0004-0004-0004-000000000005'),
('eeeeeeee-0005-0005-0005-000000000007', 'Moyenne et mediane', 'Indicateurs statistiques', 1, 'La moyenne est la somme divisee par le nombre de valeurs.', 'dddddddd-0004-0004-0004-000000000006');

INSERT INTO cours_programmer (id, cours_id, date_cours_prevue, etat_cours_programme, classe_id, lieu, description, date_creation, professeur_id) VALUES
('cp-004', 'dddddddd-0004-0004-0004-000000000005', '2025-02-05 08:00:00', 'PLANIFIE', '550e8400-e29b-41d4-a716-446655440407', 'Salle 101', 'Seance trigonometrie privee', '2025-01-20 08:00:00', '550e8400-e29b-41d4-a716-446655440007'),
('cp-005', 'dddddddd-0004-0004-0004-000000000006', '2025-02-07 08:00:00', 'PLANIFIE', '550e8400-e29b-41d4-a716-446655440407', 'Salle 101', 'Seance statistiques publique', '2025-01-20 09:00:00', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO cours_programmer_classes (cours_programmer_id, classe_id) VALUES
('cp-004', '550e8400-e29b-41d4-a716-446655440407'),
('cp-005', '550e8400-e29b-41d4-a716-446655440407');

INSERT INTO cours_programmer_participants (cours_programmer_id, utilisateur_id) VALUES
('cp-004', 'demo-eleve-0000-0000-000000000001'),
('cp-004', '550e8400-e29b-41d4-a716-446655440300'),
('cp-005', 'demo-eleve-0000-0000-000000000001'),
('cp-005', '550e8400-e29b-41d4-a716-446655440300');

INSERT INTO exercises (id, nom, description, date_creation, etat, restriction, niveau, redacteur_id) VALUES
('ffffffff-0006-0006-0006-000000000004', 'Exercice prive : Trigo', 'Exercice de trigonometrie - acces restreint', '2025-01-05 08:00:00', 'PUBLIE', 'PRIVE', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440007'),
('ffffffff-0006-0006-0006-000000000005', 'Exercice public : Stats', 'Exercice de statistiques - acces libre', '2025-01-06 08:00:00', 'PUBLIE', 'PUBLIC', 'COLLEGE', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO exercise_matieres (exercise_id, matiere_id) VALUES
('ffffffff-0006-0006-0006-000000000004', 'aaaaaaaa-0001-0001-0001-000000000001'),
('ffffffff-0006-0006-0006-000000000005', 'aaaaaaaa-0001-0001-0001-000000000001');

INSERT INTO cours_exercises (exercise_id, cours_id) VALUES
('ffffffff-0006-0006-0006-000000000004', 'dddddddd-0004-0004-0004-000000000005'),
('ffffffff-0006-0006-0006-000000000005', 'dddddddd-0004-0004-0004-000000000006');

INSERT INTO questions_reponses (id, intitule, reponse, type_question, exercise_id) VALUES
('11111111-0007-0007-0007-000000000006', 'Calculer sin(30)', '0.5', 'REPONSE_COURTE', 'ffffffff-0006-0006-0006-000000000004'),
('11111111-0007-0007-0007-000000000007', 'Calculer cos(0)', '1', 'REPONSE_COURTE', 'ffffffff-0006-0006-0006-000000000004'),
('11111111-0007-0007-0007-000000000008', 'Quelle est la moyenne de 2, 4, 6 ?', '4', 'REPONSE_COURTE', 'ffffffff-0006-0006-0006-000000000005'),
('11111111-0007-0007-0007-000000000009', 'Quelle est la mediane de 1, 3, 5 ?', '3', 'QCM', 'ffffffff-0006-0006-0006-000000000005');

INSERT INTO exercises_programmer (id, source_exercise_id, type_assignation, date_exo_prevue, date_debut_exo_effectif, date_fin_exo_effectif, etat_exercise_programmer, programme_par_id) VALUES
('ffffffff-0006-0006-0006-000000000004', 'ffffffff-0006-0006-0006-000000000004', 'DEVOIR',   '2025-02-05 09:00:00', '2025-02-05 09:00:00', '2025-02-05 10:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007'),
('ffffffff-0006-0006-0006-000000000005', 'ffffffff-0006-0006-0006-000000000005', 'EXERCICE', '2025-02-07 09:00:00', '2025-02-07 09:00:00', '2025-02-07 10:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440007');

INSERT INTO exercise_programmer_classes (exercise_programmer_id, classe_id) VALUES
('ffffffff-0006-0006-0006-000000000004', '550e8400-e29b-41d4-a716-446655440407'),
('ffffffff-0006-0006-0006-000000000005', '550e8400-e29b-41d4-a716-446655440407');

INSERT INTO participer_exo (utilisateur_id, exercise_programmer_id, etat_soumission, note, appreciation, date_debut, date_fin) VALUES
('demo-eleve-0000-0000-000000000001', 'ffffffff-0006-0006-0006-000000000004', 'EN_ATTENTE_CORRECTION', NULL, NULL,  '2025-02-05 09:05:00', '2025-02-05 09:50:00'),
('demo-eleve-0000-0000-000000000001', 'ffffffff-0006-0006-0006-000000000005', 'CORRIGE',               '19', 'Excellent', '2025-02-07 09:05:00', '2025-02-07 09:45:00');

INSERT INTO repondre (utilisateur_id, question_id, reponse_utilisateur, est_correcte, note) VALUES
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000006', '0.5', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000007', '1', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000008', '4', TRUE, '5'),
('demo-eleve-0000-0000-000000000001', '11111111-0007-0007-0007-000000000009', '3', TRUE, '5');

INSERT INTO evenements (id, titre, description, lieu, etat, heure_debut, heure_fin, createur_id, visibility) VALUES
('cccccccc-0003-0003-0003-000000000004', 'Seance revision trigo', 'Revision avant examen de trigonometrie', 'Salle 101', 'PLANIFIE', '2025-02-04 14:00:00', '2025-02-04 16:00:00', '550e8400-e29b-41d4-a716-446655440007', 'PRIVATE'),
('cccccccc-0003-0003-0003-000000000005', 'Examen blanc statistiques', 'Simulation examen statistiques', 'Salle 101', 'PLANIFIE', '2025-02-06 08:00:00', '2025-02-06 10:00:00', '550e8400-e29b-41d4-a716-446655440007', 'PRIVATE');

INSERT INTO evenement_participants (evenement_id, utilisateur_id) VALUES
('cccccccc-0003-0003-0003-000000000004', 'demo-eleve-0000-0000-000000000001'),
('cccccccc-0003-0003-0003-000000000004', '550e8400-e29b-41d4-a716-446655440300'),
('cccccccc-0003-0003-0003-000000000004', '550e8400-e29b-41d4-a716-446655440007'),
('cccccccc-0003-0003-0003-000000000005', 'demo-eleve-0000-0000-000000000001'),
('cccccccc-0003-0003-0003-000000000005', '550e8400-e29b-41d4-a716-446655440300'),
('cccccccc-0003-0003-0003-000000000005', '550e8400-e29b-41d4-a716-446655440007');

-- Link demo events to classes
INSERT INTO evenement_classes (evenement_id, classe_id) VALUES
('cccccccc-0003-0003-0003-000000000004', '550e8400-e29b-41d4-a716-446655440407'),
('cccccccc-0003-0003-0003-000000000005', '550e8400-e29b-41d4-a716-446655440407');

INSERT INTO histo_activation (id, classe_id, utilisateur_id, date_activation, is_active, etat_classe) VALUES
('aaaabbbb-0008-0008-0008-000000000004', '550e8400-e29b-41d4-a716-446655440408', '550e8400-e29b-41d4-a716-446655440007', '2024-12-01 12:00:00', TRUE, 'ACTIF');

INSERT INTO demandes_acces (id, utilisateur_id, classe_id, code_activation, etat, date_demande, date_traitement, est_parent, eleve_associe_id) VALUES
('da-005', 'demo-eleve-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440407', '111111', 'APPROUVEE', '2024-12-01 07:00:00', '2024-12-01 08:00:00', FALSE, NULL),
('da-006', 'demo-eleve-0000-0000-000000000001', '550e8400-e29b-41d4-a716-446655440408', '222222', 'APPROUVEE', '2024-12-01 07:00:00', '2024-12-01 08:00:00', FALSE, NULL);
