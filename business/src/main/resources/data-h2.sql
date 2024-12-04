INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, etat) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'Dupont', 'Jean', 'jean.dupont@example.com', 'password123', '0123456789', '123 Rue de Paris', 'active'),
('550e8400-e29b-41d4-a716-446655440001', 'Martin', 'Marie', 'marie.martin@example.com', 'password123', '0123456788', '456 Avenue de Lyon', 'active'),
('550e8400-e29b-41d4-a716-446655440002', 'Durand', 'Pierre', 'pierre.durand@example.com', 'password123', '0123456787', '789 Boulevard de Nice', 'inactive'),
('550e8400-e29b-41d4-a716-446655440003', 'Lefevre', 'Sophie', 'sophie.lefevre@example.com', 'password123', '0123456786', '101 Rue de Marseille', 'active'),
('550e8400-e29b-41d4-a716-446655440004', 'Teacher', 'Paul', 'paul.teacher@example.com', 'password123', '0123456785', '456 Rue de Nantes', 'active'), -- For Professeur
('550e8400-e29b-41d4-a716-446655440005', 'Tutor', 'Claire', 'claire.tutor@example.com', 'password123', '0123456784', '789 Rue de Lille', 'active'); -- For Repetiteur

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

INSERT INTO ressources.professeurs (professeurs_id, cni_url_front, cni_url_back, nom_etablissement, nom_classe, matricule_professeur) VALUES
('550e8400-e29b-41d4-a716-446655440004', 'https://example.com/prof-front.jpg', 'https://example.com/prof-back.jpg', 'Ecole Centrale', 'Mathématiques', 'MATPROF001');

INSERT INTO ressources.repetiteurs (repetiteurs_id, cni_url_front, cni_url_back) VALUES
('550e8400-e29b-41d4-a716-446655440005', 'https://example.com/rep-front.jpg', 'https://example.com/rep-back.jpg');
