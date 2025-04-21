-- Insert data into utilisateurs with is_admin flag
INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES
-- Add admin user
('550e8400-e29b-41d4-a716-446655440999', 'Admin', 'Super', 'admin@example.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456777', 'Admin Office', 'pepe', 'ACTIVE', TRUE),


-- Regular users (existing data)
('550e8400-e29b-41d4-a716-446655440000', 'Dupont', 'Jean', 'jean.dupont@example.com', 'password123', '0123456789', '123 Rue de Paris', 'abc123activationcode1', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440001', 'Martin', 'Marie', 'marie.martin@example.com', 'password123', '0123456788', '456 Avenue de Lyon', 'abc123activationcode2', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440002', 'Durand', 'Pierre', 'pierre.durand@example.com', 'password123', '0123456787', '789 Boulevard de Nice', 'abc123activationcode3', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440003', 'Lefevre', 'Sophie', 'sophie.lefevre@example.com', 'password123', '0123456786', '101 Rue de Marseille', 'abc123activationcode4', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440004', 'Durand', 'Paul', 'paul.durand@example.com', 'password123', '0123456785', '111 Rue de Lille', 'abc123activationcode5', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440007', 'Marie', 'Dupont', 'marie.dupont@example.com', 'password123', '0123456789', '123 Rue de Paris', 'abc123activationcode6', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440008', 'Lucas', 'Martin', 'lucas.martin@example.com', 'password123', '0123456788', '456 Avenue de Lyon', 'abc123activationcode7', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440009', 'Isabelle', 'Lefevre', 'isabelle.lefevre@example.com', 'password123', '0123456787', '789 Boulevard de Nice', 'abc123activationcode8', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440010', 'Paul', 'Durand', 'paul.durand@example.com', 'password123', '0123456786', '111 Rue de Lille', 'abc123activationcode9', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440200', 'Parent A', 'FirstNameA', 'parenta@example.com', 'password123', '0123456780', 'AddressA', 'abc123activationcode10', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440201', 'Parent B', 'FirstNameB', 'parentb@example.com', 'password123', '0123456781', 'AddressB', 'abc123activationcode11', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440300', 'Eleve A', 'Jean', 'jean.elevea@example.com', 'password123', '0123456700', '10 Rue des Écoles', 'abc123activationcode12', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440301', 'Eleve B', 'Marie', 'marie.eleveb@example.com', 'password123', '0123456701', '20 Rue des Lycées', 'abc123activationcode13', 'ACTIVE', FALSE),
('550e8400-e29b-41d4-a716-446655440302', 'Eleve C', 'Paul', 'paul.elevec@example.com', 'password123', '0123456702', '30 Boulevard Université', 'abc123activationcode14', 'ACTIVE', FALSE);
-- Insert data into messages

-- Insertion des motifs de rejet initiaux
-- Add test professor with your email
INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES
    ('660e8400-e29b-41d4-a716-446655440999', 'Test', 'Professor', 'ulrichkamsu48@gmail.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456789', 'Test Address', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluQGV4YW1wbGUuY29tIiwiaWF0IjoxNzQ0OTY3MzM1LCJleHAiOjE3NDQ5NjgyMzV9.NVeY4KP8KAM2Nh80NaFXYEJ4__ceTFOPQPe_pGryMQw', 'AWAITING_VALIDATION', FALSE);

INSERT INTO ressources.professeurs (professeurs_id, cni_url_front, cni_url_back, nom_etablissement, nom_classe, matricule_professeur) VALUES
    ('660e8400-e29b-41d4-a716-446655440999', 'http://example.com/cni.jpg', 'http://example.com/cni-back.jpg', 'Test School', 'Test Class', 'PROF-TEST-001');
-- Insert initial rejection reasons
INSERT INTO ressources.messages (id, contenu, datecreation, datemodification, etat, expediteur_id) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'Bonjour, comment ça va?', '2023-10-01', '2023-10-01', 'envoyé', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440014', 'Réunion à 15h après demain.', '2024-10-02', '2023-10-02', 'envoyé', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440011', 'Réunion à 10h demain.', '2023-10-02', '2023-10-02', 'envoyé', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440012', 'Merci pour votre aide.', '2023-10-03', '2023-10-03', 'envoyé', '550e8400-e29b-41d4-a716-446655440002'),
('550e8400-e29b-41d4-a716-446655440013', 'Veuillez trouver ci-joint le document.', '2023-10-04', '2023-10-04', 'envoyé', '550e8400-e29b-41d4-a716-446655440003');

-- Insert data into recevoir
INSERT INTO ressources.recevoir (message_id, utilisateur_id) VALUES
('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440014', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440003'),
('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440003'),
('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440003'),
('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440002'),
('550e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440002');

-- Insert data into etablissements
INSERT INTO ressources.etablissements (id, nom) VALUES
('550e8400-e29b-41d4-a716-446655440100', 'Etablissement A'),
('550e8400-e29b-41d4-a716-446655440101', 'Etablissement B');

-- Insert data into parents
INSERT INTO ressources.parents (parents_id) VALUES
('550e8400-e29b-41d4-a716-446655440200'),
('550e8400-e29b-41d4-a716-446655440201');

-- Insert data into eleves
INSERT INTO ressources.eleves (eleves_id, niveau) VALUES
('550e8400-e29b-41d4-a716-446655440300','6eme'),
('550e8400-e29b-41d4-a716-446655440301','5eme');

-- Insert data into classes
INSERT INTO ressources.classes (id, nom, niveau, date_creation, etat, etablissement_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', 'Classe A', 'Niveau 1', '2024-11-28 10:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440100');

-- Associate parents with classes
INSERT INTO ressources.classe_parents (classe_id, parent_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440200');

-- Associate eleves with classes
INSERT INTO ressources.classe_eleves (classe_id, eleve_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440300');

-- Insert data into professeurs
INSERT INTO ressources.professeurs (professeurs_id, cni_url_front, cni_url_back, nom_etablissement, nom_classe, matricule_professeur) VALUES
('550e8400-e29b-41d4-a716-446655440007', 'https://example.com/cni/marie_dupont_front.jpg', 'https://example.com/cni/marie_dupont_back.jpg', 'Lycée Jean Moulin', 'Terminale A', 'PROF-2024-001'),
('550e8400-e29b-41d4-a716-446655440008', 'https://example.com/cni/lucas_martin_front.jpg', 'https://example.com/cni/lucas_martin_back.jpg', 'Collège Albert Camus', 'Quatrième B', 'PROF-2024-002'),
('550e8400-e29b-41d4-a716-446655440009', 'https://example.com/cni/isabelle_lefevre_front.jpg', 'https://example.com/cni/isabelle_lefevre_back.jpg', 'École Primaire Victor Hugo', 'CE2', 'PROF-2024-003');

-- Insert data into repetiteurs
INSERT INTO ressources.repetiteurs (repetiteurs_id, cni_url_front, cni_url_back, photo_full_picture, nom_classe) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'https://example.com/cni/paul_durand_front.jpg', 'https://example.com/cni/paul_durand_back.jpg', 'https://example.com/photos/paul_durand_full.jpg', 'Terminale B');


INSERT INTO ressources.motifs_rejet (id, code, descriptif, date_creation) VALUES
  ('550e8400-e29b-41d4-a716-446655440600', 'PHOTO_FLOU_RECTO', 'Photo recto de la CNI floue ou illisible', CURRENT_TIMESTAMP),
  ('550e8400-e29b-41d4-a716-446655440601', 'PHOTO_FLOU_VERSO', 'Photo verso de la CNI floue ou illisible', CURRENT_TIMESTAMP),
  ('550e8400-e29b-41d4-a716-446655440602', 'PHOTO_FLOU_SELFIE', 'Photo selfie floue ou illisible', CURRENT_TIMESTAMP),
  ('550e8400-e29b-41d4-a716-446655440603', 'PHOTO_INCOHERENTE_RECTO_VERSO', 'Incohérence entre les photos recto et verso de la CNI', CURRENT_TIMESTAMP),
  ('550e8400-e29b-41d4-a716-446655440604', 'PHOTO_INCOHERENTE_RECTO_SELFIE', 'Incohérence entre la photo recto de la CNI et le selfie', CURRENT_TIMESTAMP),
  ('550e8400-e29b-41d4-a716-446655440605', 'MATRICULE_INCORRECT', 'Matricule professeur incorrect ou invalide', CURRENT_TIMESTAMP);
-- Insert data into canaux
INSERT INTO ressources.canaux (id, nom, description, professeur_id, classe_id) VALUES
('550e8400-e29b-41d4-a716-446655440500', 'Canal de Mathématiques', 'Canal dédié aux cours de mathématiques', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440400'),
('550e8400-e29b-41d4-a716-446655440501', 'Canal de Français', 'Canal dédié aux cours de français', '550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440400'),
('550e8400-e29b-41d4-a716-446655440502', 'Canal de Sciences', 'Canal dédié aux cours de sciences', '550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440400');
