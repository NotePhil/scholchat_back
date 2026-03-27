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
