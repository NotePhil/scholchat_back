INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, etat) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Dupont', 'Jean', 'jean.dupont@example.com', 'password123', '0123456789', '123 Rue de Paris', 'active'),
('550e8400-e29b-41d4-a716-446655440001', 'Martin', 'Marie', 'marie.martin@example.com', 'password123', '0123456788', '456 Avenue de Lyon', 'active'),
('550e8400-e29b-41d4-a716-446655440002', 'Durand', 'Pierre', 'pierre.durand@example.com', 'password123', '0123456787', '789 Boulevard de Nice', 'inactive'),
('550e8400-e29b-41d4-a716-446655440003', 'Lefevre', 'Sophie', 'sophie.lefevre@example.com', 'password123', '0123456786', '101 Rue de Marseille', 'active');

INSERT INTO ressources.messages (id, contenu, datecreation, datemodification, etat, expediteur_id) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'Bonjour, comment ça va?', '2023-10-01', '2023-10-01', 'envoyé', '550e8400-e29b-41d4-a716-446655440000'),
('550e8400-e29b-41d4-a716-446655440014', 'Réunion à 15h après demain.', '2024-10-02', '2023-10-02', 'envoyé', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440011', 'Réunion à 10h demain.', '2023-10-02', '2023-10-02', 'envoyé', '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440012', 'Merci pour votre aide.', '2023-10-03', '2023-10-03', 'envoyé', '550e8400-e29b-41d4-a716-446655440002'),
('550e8400-e29b-41d4-a716-446655440013', 'Veuillez trouver ci-joint le document.', '2023-10-04', '2023-10-04', 'envoyé', '550e8400-e29b-41d4-a716-446655440003');

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

-- Insert sample data for Etablissements
INSERT INTO ressources.etablissements (id, nom) VALUES
('550e8400-e29b-41d4-a716-446655440100', 'Etablissement A'),
('550e8400-e29b-41d4-a716-446655440101', 'Etablissement B');

-- Insert sample data for Parents
INSERT INTO ressources.parents (id, nom) VALUES
('550e8400-e29b-41d4-a716-446655440200', 'Parent A'),
('550e8400-e29b-41d4-a716-446655440201', 'Parent B');

-- Insert sample data for Eleves
INSERT INTO ressources.eleves (id, nom) VALUES
('550e8400-e29b-41d4-a716-446655440300', 'Eleve A'),
('550e8400-e29b-41d4-a716-446655440301', 'Eleve B');

-- Insert sample data for Classes
INSERT INTO ressources.classes (id, nom, niveau, date_creation, etat, etablissement_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', 'Classe A', 'Niveau 1', '2024-11-28 10:00:00', 'ACTIF', '550e8400-e29b-41d4-a716-446655440100');

-- Associate Parents with Classes
INSERT INTO ressources.classe_parents (classe_id, parent_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440200');

-- Associate Eleves with Classes
INSERT INTO ressources.classe_eleves (classe_id, eleve_id) VALUES
('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440300');

-- Then insert corresponding professeurs records
INSERT INTO ressources.professeurs (professeurs_id, cni_url_front, cni_url_back, nom_etablissement, nom_classe, matricule_professeur) VALUES
('550e8400-e29b-41d4-a716-446655440004', 'https://example.com/cni/marie_dupont_front.jpg', 'https://example.com/cni/marie_dupont_back.jpg', 'Lycée Jean Moulin', 'Terminale A', 'PROF-2024-001'),
('550e8400-e29b-41d4-a716-446655440005', 'https://example.com/cni/lucas_martin_front.jpg', 'https://example.com/cni/lucas_martin_back.jpg', 'Collège Albert Camus', 'Quatrième B', 'PROF-2024-002'),
('550e8400-e29b-41d4-a716-446655440006', 'https://example.com/cni/isabelle_lefevre_front.jpg', 'https://example.com/cni/isabelle_lefevre_back.jpg', 'École Primaire Victor Hugo', 'CE2', 'PROF-2024-003');