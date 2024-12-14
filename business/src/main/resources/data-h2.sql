-- Insert data into utilisateurs
INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, etat) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Dupont', 'Jean', 'jean.dupont@example.com', 'password123', '0123456789', '123 Rue de Paris', 'active'),
('550e8400-e29b-41d4-a716-446655440001', 'Martin', 'Marie', 'marie.martin@example.com', 'password123', '0123456788', '456 Avenue de Lyon', 'active'),
('550e8400-e29b-41d4-a716-446655440002', 'Durand', 'Pierre', 'pierre.durand@example.com', 'password123', '0123456787', '789 Boulevard de Nice', 'inactive'),
('550e8400-e29b-41d4-a716-446655440003', 'Lefevre', 'Sophie', 'sophie.lefevre@example.com', 'password123', '0123456786', '101 Rue de Marseille', 'active'),
('550e8400-e29b-41d4-a716-446655440004', 'Durand', 'Paul', 'paul.durand@example.com', 'password123', '0123456785', '111 Rue de Lille', 'active'),
('550e8400-e29b-41d4-a716-446655440007', 'Marie', 'Dupont', 'marie.dupont@example.com', 'password123', '0123456789', '123 Rue de Paris', 'active'),
('550e8400-e29b-41d4-a716-446655440008', 'Lucas', 'Martin', 'lucas.martin@example.com', 'password123', '0123456788', '456 Avenue de Lyon', 'active'),
('550e8400-e29b-41d4-a716-446655440009', 'Isabelle', 'Lefevre', 'isabelle.lefevre@example.com', 'password123', '0123456787', '789 Boulevard de Nice', 'active'),
('550e8400-e29b-41d4-a716-446655440010', 'Paul', 'Durand', 'paul.durand@example.com', 'password123', '0123456786', '111 Rue de Lille', 'active'),
('550e8400-e29b-41d4-a716-446655440200', 'Parent A', 'FirstNameA', 'parenta@example.com', 'password123', '0123456780', 'AddressA', 'active'),
('550e8400-e29b-41d4-a716-446655440201', 'Parent B', 'FirstNameB', 'parentb@example.com', 'password123', '0123456781', 'AddressB', 'active'),
('550e8400-e29b-41d4-a716-446655440300', 'Eleve A', 'Jean', 'jean.elevea@example.com', 'password123', '0123456700', '10 Rue des Écoles', 'active'),
('550e8400-e29b-41d4-a716-446655440301', 'Eleve B', 'Marie', 'marie.eleveb@example.com', 'password123', '0123456701', '20 Rue des Lycées', 'active'),
('550e8400-e29b-41d4-a716-446655440302', 'Eleve C', 'Paul', 'paul.elevec@example.com', 'password123', '0123456702', '30 Boulevard Université', 'inactive');


-- Insert data into messages
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
INSERT INTO ressources.parents (id, nom) VALUES
('550e8400-e29b-41d4-a716-446655440200', 'Parent A'),
('550e8400-e29b-41d4-a716-446655440201', 'Parent B');

-- Insert data into eleves
INSERT INTO ressources.eleves (id, nom) VALUES
('550e8400-e29b-41d4-a716-446655440300', 'Eleve A'),
('550e8400-e29b-41d4-a716-446655440301', 'Eleve B');

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

-- Insert data into profil_parents
INSERT INTO ressources.profil_parents (profil_parents_id, cni_url_front, cni_url_back, full_pic_url) VALUES
('550e8400-e29b-41d4-a716-446655440200', 'https://example.com/cni/parent_a_front.jpg', 'https://example.com/cni/parent_a_back.jpg', 'https://example.com/photos/parent_a_full.jpg'),
('550e8400-e29b-41d4-a716-446655440201', 'https://example.com/cni/parent_b_front.jpg', 'https://example.com/cni/parent_b_back.jpg', 'https://example.com/photos/parent_b_full.jpg');
INSERT INTO ressources.profil_eleves (profil_eleves_id) VALUES
('550e8400-e29b-41d4-a716-446655440300'),
('550e8400-e29b-41d4-a716-446655440301'),
('550e8400-e29b-41d4-a716-446655440302');