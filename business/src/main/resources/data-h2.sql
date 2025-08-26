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
    ('550e8400-e29b-41d4-a716-446655440302', 'Eleve C', 'Paul', 'paul.elevec@example.com', 'password123', '0123456702', '30 Boulevard Université', 'abc123activationcode14', 'ACTIVE', FALSE);
    -- Insert data into messages

    -- Add test professor with your email
    INSERT INTO ressources.utilisateurs (id, nom, prenom, email, passeaccess, telephone, adresse, activation_token, etat, is_admin) VALUES
        ('660e8400-e29b-41d4-a716-446655440999', 'Test', 'Professor', 'ulrichkamsu48@gmail.com', '$2a$10$DyP2uVCelVt3OJnRXs.A2Oa30GyPINfeaKSlCnwYt8uHMiVkn2BDO', '0123456789', 'Test Address', 'eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInN1YiI6ImFkbWluQGV4YW1wbGUuY29tIiwiaWF0IjoxNzQ0OTY3MzM1LCJleHAiOjE3NDQ5NjgyMzV9.NVeY4KP8KAM2Nh80NaFXYEJ4__ceTFOPQPe_pGryMQw', 'AWAITING_VALIDATION', FALSE);

    -- Insert schools
    INSERT INTO ressources.etablissements (id, nom, localisation, pays, email, telephone, option_envoi_mail_classe, option_token_general, code_unique) VALUES
    ('550e8400-e29b-41d4-a716-446655440100', 'Etablissement A', 'Yaoundé', 'Cameroun', 'contact@etab-a.cm', '23712345678', TRUE, FALSE, TRUE),
    ('550e8400-e29b-41d4-a716-446655440101', 'Etablissement B', 'Douala', 'Cameroun', 'info@etab-b.cm', '23787654321', FALSE, TRUE, FALSE);

    -- Insert professors (must come after users)
    INSERT INTO ressources.professeurs (professeurs_id, cni_url_front, cni_url_back, selfie_url, matricule_professeur) VALUES
    ('550e8400-e29b-41d4-a716-446655440007', 'https://example.com/cni/marie_dupont_front.jpg', 'https://example.com/cni/marie_dupont_back.jpg', 'https://example.com/selfie/marie_dupont_selfie.jpg', 'PROF-2024-001'),
    ('550e8400-e29b-41d4-a716-446655440008', 'https://example.com/cni/lucas_martin_front.jpg', 'https://example.com/cni/lucas_martin_back.jpg', NULL, 'PROF-2024-002'),
    ('550e8400-e29b-41d4-a716-446655440009', 'https://example.com/cni/isabelle_lefevre_front.jpg', 'https://example.com/cni/isabelle_lefevre_back.jpg', 'https://example.com/selfie/isabelle_lefevre_selfie.jpg', NULL),
    ('660e8400-e29b-41d4-a716-446655440999', 'http://example.com/cni.jpg', 'http://example.com/cni-back.jpg', 'http://example.com/selfie.jpg', 'PROF-TEST-001');

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
    INSERT INTO ressources.classes (id, nom, niveau, date_creation, code_activation, etat, etablissement_id, moderator_id, acces_majeur) VALUES
    ('550e8400-e29b-41d4-a716-446655440400', 'Classe A', '3ème', '2024-11-28 08:00:00', '123456', 'ACTIF', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440007',FALSE),
    ('550e8400-e29b-41d4-a716-446655440401', 'Classe B', '2nde', '2024-11-28 09:00:00', '234567', 'ACTIF', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440008',TRUE),
    ('550e8400-e29b-41d4-a716-446655440402', 'Classe C - Pending', '4ème', '2024-11-29 09:00:00', '789012', 'EN_ATTENTE_APPROBATION', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440009',FALSE),
    ('550e8400-e29b-41d4-a716-446655440403', 'Classe D - Pending', '1ère', '2024-11-29 10:00:00', '890123', 'EN_ATTENTE_APPROBATION', '550e8400-e29b-41d4-a716-446655440101', '660e8400-e29b-41d4-a716-446655440999',TRUE),
    ('550e8400-e29b-41d4-a716-446655440404', 'Classe E - Inactive', '4ème', '2024-11-30 08:00:00', '901234', 'INACTIF', '550e8400-e29b-41d4-a716-446655440100', '550e8400-e29b-41d4-a716-446655440007',FALSE),
    ('550e8400-e29b-41d4-a716-446655440405', 'Classe F - Inactive', 'Terminale', '2024-11-30 09:00:00', '012345', 'INACTIF', '550e8400-e29b-41d4-a716-446655440101', '550e8400-e29b-41d4-a716-446655440008',FALSE);

    -- Now we can insert class relationships
    INSERT INTO ressources.classe_parents (classe_id, parent_id) VALUES
    ('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440200'),
    ('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440201');

    INSERT INTO ressources.classe_eleves (classe_id, eleve_id) VALUES
    ('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440300'),
    ('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440301'),
    ('550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440302');

    -- Insert moderator relationships
    INSERT INTO ressources.professeur_classes_moderees (professeur_id, classe_id) VALUES
    ('550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440402'),
    ('660e8400-e29b-41d4-a716-446655440999', '550e8400-e29b-41d4-a716-446655440403'),
    ('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440404'),
    ('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440405');

    -- Insert subjects
    INSERT INTO ressources.matieres (id, nom, description, date_creation, etat) VALUES
    ('550e8400-e29b-41d4-a716-446655441000', 'MATHEMATIQUES', 'Cours de mathématiques avancées', '2024-01-01 09:00:00', 'ACTIF'),
    ('550e8400-e29b-41d4-a716-446655441001', 'SCIENCES', 'Sciences physiques et naturelles', '2024-01-01 09:00:00', 'ACTIF'),
    ('550e8400-e29b-41d4-a716-446655441002', 'HISTOIRE', 'Histoire générale et du Cameroun', '2024-01-01 09:00:00', 'ACTIF'),
    ('550e8400-e29b-41d4-a716-446655441003', 'GEOGRAPHIE', 'Géographie mondiale et régionale', '2024-01-01 09:00:00', 'ACTIF'),
    ('550e8400-e29b-41d4-a716-446655441004', 'LANGUE', 'Langues et littérature', '2024-01-01 09:00:00', 'ACTIF');

    -- Insert class-subject associations
    INSERT INTO ressources.classe_matieres (matiere_id, classe_id) VALUES
    ('550e8400-e29b-41d4-a716-446655441000', '550e8400-e29b-41d4-a716-446655440400'),
    ('550e8400-e29b-41d4-a716-446655441001', '550e8400-e29b-41d4-a716-446655440400'),
    ('550e8400-e29b-41d4-a716-446655441002', '550e8400-e29b-41d4-a716-446655440401'),
    ('550e8400-e29b-41d4-a716-446655441003', '550e8400-e29b-41d4-a716-446655440401');

    -- Insert professor-subject associations
    INSERT INTO ressources.professeur_matiere (professeur_id, matiere_id) VALUES
    ('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655441000'),
    ('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655441001'),
    ('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655441002'),
    ('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655441003'),
    ('550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655441004');

    -- Insert communication channels
    INSERT INTO ressources.canaux (id, nom, description, professeur_id, classe_id) VALUES
    ('550e8400-e29b-41d4-a716-446655440500', 'Canal de Mathématiques', 'Canal dédié aux cours de mathématiques', '550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440400'),
    ('550e8400-e29b-41d4-a716-446655440501', 'Canal de Français', 'Canal dédié aux cours de français', '550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440401'),
    ('550e8400-e29b-41d4-a716-446655440502', 'Canal de Sciences', 'Canal dédié aux cours de sciences', '550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440400');

    -- Insert rejection reasons
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

    -- Insert events
    INSERT INTO ressources.evenements (id, titre, description, lieu, etat, heure_debut, heure_fin, createur_id) VALUES
    ('550e8400-e29b-41d4-a716-446655442000', 'Réunion parents-professeurs', 'Réunion trimestrielle', 'Salle de réunion', 'PLANIFIE', '2024-12-15 14:00:00', '2024-12-15 16:00:00', '550e8400-e29b-41d4-a716-446655440007'),
    ('550e8400-e29b-41d4-a716-446655442001', 'Sortie pédagogique', 'Visite du musée national', 'Musée National', 'PLANIFIE', '2024-12-20 09:00:00', '2024-12-20 17:00:00', '550e8400-e29b-41d4-a716-446655440008');

    -- Insert event participants
    INSERT INTO ressources.evenement_participants (evenement_id, utilisateur_id) VALUES
    ('550e8400-e29b-41d4-a716-446655442000', '550e8400-e29b-41d4-a716-446655440200'),
    ('550e8400-e29b-41d4-a716-446655442000', '550e8400-e29b-41d4-a716-446655440300'),
    ('550e8400-e29b-41d4-a716-446655442001', '550e8400-e29b-41d4-a716-446655440201'),
    ('550e8400-e29b-41d4-a716-446655442001', '550e8400-e29b-41d4-a716-446655440301');

    -- Insert messages
    INSERT INTO ressources.messages (id, contenu, datecreation, datemodification, etat, expediteur_id) VALUES
    ('550e8400-e29b-41d4-a716-446655440010', 'Bonjour, comment ça va?', '2023-10-01', '2023-10-01', 'envoyé', '550e8400-e29b-41d4-a716-446655440999'),
    ('550e8400-e29b-41d4-a716-446655440011', 'Réunion à 10h demain.', '2023-10-02', '2023-10-02', 'envoyé', '550e8400-e29b-41d4-a716-446655440001'),
    ('550e8400-e29b-41d4-a716-446655440012', 'Merci pour votre aide.', '2023-10-03', '2023-10-03', 'envoyé', '550e8400-e29b-41d4-a716-446655440002'),
    ('550e8400-e29b-41d4-a716-446655440013', 'Veuillez trouver ci-joint le document.', '2023-10-04', '2023-10-04', 'envoyé', '550e8400-e29b-41d4-a716-446655440003'),
    ('550e8400-e29b-41d4-a716-446655440014', 'Réunion à 15h après demain.', '2024-10-02', '2023-10-02', 'envoyé', '550e8400-e29b-41d4-a716-446655440001');

    -- Insert message recipients
    INSERT INTO ressources.recevoir (message_id, utilisateur_id) VALUES
    ('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440001'),
    ('550e8400-e29b-41d4-a716-446655440010', '550e8400-e29b-41d4-a716-446655440003'),
    ('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440000'),
    ('550e8400-e29b-41d4-a716-446655440011', '550e8400-e29b-41d4-a716-446655440003'),
    ('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440002'),
    ('550e8400-e29b-41d4-a716-446655440012', '550e8400-e29b-41d4-a716-446655440003'),
    ('550e8400-e29b-41d4-a716-446655440013', '550e8400-e29b-41d4-a716-446655440002'),
    ('550e8400-e29b-41d4-a716-446655440014', '550e8400-e29b-41d4-a716-446655440999');

    -- Insert media
    INSERT INTO ressources.media (id, bucket_name, content_type, file_name, file_path, file_size, file_type, media_type, owner_id, uploaded_date, evenement_id) VALUES
    ('550e8400-e29b-41d4-a716-446655443000', 'school-bucket', 'image/jpeg', 'math_course.jpg', 'courses/math/2024/math_course.jpg', 1024, 'IMAGE', 'COURSE_MATERIAL', '550e8400-e29b-41d4-a716-446655440007', CURRENT_TIMESTAMP, NULL),
    ('550e8400-e29b-41d4-a716-446655443001', 'school-bucket', 'application/pdf', 'history_syllabus.pdf', 'courses/history/2024/syllabus.pdf', 2048, 'DOCUMENT', 'SYLLABUS', '550e8400-e29b-41d4-a716-446655440008', CURRENT_TIMESTAMP, NULL),
    ('550e8400-e29b-41d4-a716-446655443002', 'events-bucket', 'image/png', 'event_poster.png', 'events/2024/parent_meeting.png', 3072, 'IMAGE', 'EVENT_MATERIAL', '550e8400-e29b-41d4-a716-446655440007', CURRENT_TIMESTAMP, '550e8400-e29b-41d4-a716-446655442000');

    -- Insert interactions
    INSERT INTO ressources.interactions (id, type, content, creation_date, niveau, created_by, event_id, message_id) VALUES
    ('550e8400-e29b-41d4-a716-446655444000', 'COMMENT', 'Excellent cours aujourd''hui!', CURRENT_TIMESTAMP, 'POSITIVE', '550e8400-e29b-41d4-a716-446655440300', NULL, NULL),
    ('550e8400-e29b-41d4-a716-446655444001', 'QUESTION', 'Quand sera disponible le prochain devoir?', CURRENT_TIMESTAMP, 'NEUTRAL', '550e8400-e29b-41d4-a716-446655440301', NULL, NULL),
    ('550e8400-e29b-41d4-a716-446655444002', 'FEEDBACK', 'La réunion était très informative', CURRENT_TIMESTAMP, 'POSITIVE', '550e8400-e29b-41d4-a716-446655440200', '550e8400-e29b-41d4-a716-446655442000', NULL);

    -- Insert authentication tokens
    INSERT INTO ressources.refresh_tokens (token, expiry_date, utilisateur_id) VALUES
    ('eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDA5OTkiLCJpYXQiOjE2MTYyMzkwMjIsImV4cCI6MTYxNjMyNTQyMn0.4j5X9v2JwQ7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q', '2025-12-31 23:59:59', '550e8400-e29b-41d4-a716-446655440999'),
    ('eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2NjBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDA5OTkiLCJpYXQiOjE2MTYyMzkwMjIsImV4cCI6MTYxNjMyNTQyMn0.4j5X9v2JwQ7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q7q7Q', '2025-12-31 23:59:59', '660e8400-e29b-41d4-a716-446655440999');

    -- Insert activation history
    INSERT INTO ressources.histo_activation
    (id, classe_id, utilisateur_id, date_activation, date_desactivation, is_active, etat_classe, motif_desactivation)
    VALUES
    ('550e8400-e29b-41d4-a716-446655445000', '550e8400-e29b-41d4-a716-446655440400', '550e8400-e29b-41d4-a716-446655440007', '2024-11-28 08:00:00', NULL, TRUE, 'ACTIF', NULL),
    ('550e8400-e29b-41d4-a716-446655445001', '550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440008', '2024-11-28 09:00:00', NULL, TRUE, 'ACTIF', NULL),
    ('550e8400-e29b-41d4-a716-446655445002', '550e8400-e29b-41d4-a716-446655440402', '550e8400-e29b-41d4-a716-446655440009', '2024-11-29 09:00:00', '2024-11-29 10:00:00', FALSE, 'INACTIF', 'Class ended'),
    ('550e8400-e29b-41d4-a716-446655445003', '550e8400-e29b-41d4-a716-446655440403', '660e8400-e29b-41d4-a716-446655440999', '2024-11-29 10:00:00', '2024-11-29 11:00:00', FALSE, 'INACTIF', 'Class cancelled'),
    ('550e8400-e29b-41d4-a716-446655445004', '550e8400-e29b-41d4-a716-446655440404', '550e8400-e29b-41d4-a716-446655440007', '2024-11-20 08:00:00', '2024-11-20 09:00:00', FALSE, 'INACTIF', 'Temporary closure'),
    ('550e8400-e29b-41d4-a716-446655445005', '550e8400-e29b-41d4-a716-446655440405', '550e8400-e29b-41d4-a716-446655440008', '2024-11-21 09:00:00', '2024-11-21 10:00:00', FALSE, 'INACTIF', 'Season ended'),
    ('660e8400-e29b-41d4-a716-446655445999', '550e8400-e29b-41d4-a716-446655440400', '660e8400-e29b-41d4-a716-446655440999', '2024-12-01 09:00:00', NULL, TRUE, 'ACTIF', NULL);
    INSERT INTO ressources.acceder (utilisateur_id, classe_id, date_acces) VALUES
    -- Active users in active classes
    -- ('550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440400', '2024-01-01 09:00:00'), -- User 1 in Class A
    ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440401', '2024-01-01 09:00:00'), -- User 2 in Class B
    ('550e8400-e29b-41d4-a716-446655440999', '550e8400-e29b-41d4-a716-446655440400', '2024-01-01 09:00:00'), -- User 3 in Class A
    ('550e8400-e29b-41d4-a716-446655440999', '550e8400-e29b-41d4-a716-446655440401', '2024-01-01 09:00:00'),
    ('550e8400-e29b-41d4-a716-446655440300', '550e8400-e29b-41d4-a716-446655440400', '2024-01-01 09:00:00'),
    ('550e8400-e29b-41d4-a716-446655440300', '550e8400-e29b-41d4-a716-446655440401', '2024-01-01 09:00:00'), -- User 2 in Class B
        ('550e8400-e29b-41d4-a716-446655440009', '550e8400-e29b-41d4-a716-446655440400', '2024-01-01 09:00:00'), -- User 3 in Class A
    ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440400', '2024-01-01 09:00:00'), -- User 3 in Class A
    ('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440400', '2024-01-01 09:00:00'), -- Professor Marie Dupont to Class A
    ('550e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440401', '2024-01-01 09:00:00'); -- Professor Lucas Martin to Class B

    VALUES ('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440401', TRUE, TRUE);
    -- Insert access requests
    INSERT INTO ressources.demandes_acces (id, utilisateur_id, classe_id, code_activation, etat, date_demande) VALUES
    -- Pending requests
    ('660e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440000', '550e8400-e29b-41d4-a716-446655440400', '123456', 'APPROUVEE', '2024-01-01 10:00:00'),
    ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440401', '234567', 'APPROUVEE', '2024-01-01 11:00:00'),

    -- Approved request
    ('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440400', '123456', 'APPROUVEE', '2024-01-01 12:00:00'),

    -- Rejected request
    ('660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440401', '234567', 'REJETEE', '2024-01-01 13:00:00');
    -- Insert publication rights for kpgpa237@gmail.com (Marie Dupont)
    INSERT INTO ressources.droit_publication (utilisateur_id, classe_id, date_attribution, peut_publier, peut_moderer) VALUES
    ('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440400', '2024-01-01 09:00:00', TRUE, FALSE), -- Class A (can publish)
    ('550e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440401', '2024-01-01 09:00:00', TRUE, TRUE); -- Class B (can publish and moderate)
    INSERT INTO ressources.parent_eleve (parent_id, eleve_id)
    SELECT '550e8400-e29b-41d4-a716-446655440200', '550e8400-e29b-41d4-a716-446655440300'
    WHERE NOT EXISTS (
        SELECT 1 FROM ressources.parent_eleve
        WHERE parent_id = '550e8400-e29b-41d4-a716-446655440200'
        AND eleve_id = '550e8400-e29b-41d4-a716-446655440300'
    );

    INSERT INTO ressources.parent_eleve (parent_id, eleve_id)
    SELECT '550e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440301'
    WHERE NOT EXISTS (
        SELECT 1 FROM ressources.parent_eleve
        WHERE parent_id = '550e8400-e29b-41d4-a716-446655440201'
        AND eleve_id = '550e8400-e29b-41d4-a716-446655440301'
    );

    -- Insert sample courses for professor '550e8400-e29b-41d4-a716-446655440007'
    INSERT INTO ressources.cours (id, titre, description, date_creation, etat, references, contenu, redacteur_id) VALUES
    ('660e8400-e29b-41d4-a716-446655441100', 'Introduction aux Mathématiques', 'Cours d''introduction aux concepts mathématiques de base', '2024-01-15 09:00:00', 'PUBLIE', 'Référence 1, Référence 2', 'Contenu détaillé du cours d''introduction...', '550e8400-e29b-41d4-a716-446655440007'),
    ('660e8400-e29b-41d4-a716-446655441101', 'Algèbre Linéaire', 'Cours avancé sur les matrices et espaces vectoriels', '2024-02-01 10:00:00', 'PUBLIE', 'Référence 3, Référence 4', 'Contenu détaillé du cours d''algèbre...', '550e8400-e29b-41d4-a716-446655440007'),
    ('660e8400-e29b-41d4-a716-446655441102', 'Géométrie Euclidienne', 'Cours sur les principes de la géométrie classique', '2024-02-15 11:00:00', 'BROUILLON', 'Référence 5', 'Contenu en préparation...', '550e8400-e29b-41d4-a716-446655440007'),
    ('660e8400-e29b-41d4-a716-446655441103', 'Physique Quantique', 'Introduction aux concepts de physique quantique', '2024-03-01 14:00:00', 'EN_ATTENTE_VALIDATION', 'Référence 6, Référence 7', 'Contenu du cours de physique...', '550e8400-e29b-41d4-a716-446655440007');

    -- Associate courses with subjects (MATHEMATIQUES and SCIENCES)
    INSERT INTO ressources.cours_matiere (cours_id, matiere_id) VALUES
    ('660e8400-e29b-41d4-a716-446655441100', '550e8400-e29b-41d4-a716-446655441000'), -- Mathématiques
    ('660e8400-e29b-41d4-a716-446655441101', '550e8400-e29b-41d4-a716-446655441000'), -- Mathématiques
    ('660e8400-e29b-41d4-a716-446655441102', '550e8400-e29b-41d4-a716-446655441000'), -- Mathématiques
    ('660e8400-e29b-41d4-a716-446655441103', '550e8400-e29b-41d4-a716-446655441001'); -- Sciences

    -- Schedule some courses for Class A ('550e8400-e29b-41d4-a716-446655440400')
--     INSERT INTO ressources.cours_programmer
--     (id, cours_id, professeur_id, date_cours_prevue, date_debut_effectif, date_fin_effectif, etat_cours_programme, classe_id, lieu, description, date_creation, date_modification)
--     VALUES
--         ('770e8400-e29b-41d4-a716-446655442100', '660e8400-e29b-41d4-a716-446655441100', '550e8400-e29b-41d4-a716-446655440007', '2024-04-10 08:00:00', '2024-04-10 08:05:00', '2024-04-10 09:35:00', 'TERMINE', '550e8400-e29b-41d4-a716-446655440400', 'Salle A1', 'Cours introductif aux mathématiques', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

--     -- Add student participation to scheduled courses
--     INSERT INTO ressources.cours_programmer_participants (cours_programmer_id, utilisateur_id) VALUES
--     ('770e8400-e29b-41d4-a716-446655442100', '550e8400-e29b-41d4-a716-446655440300'), -- Eleve A
--     ('770e8400-e29b-41d4-a716-446655442100', '550e8400-e29b-41d4-a716-446655440302'), -- Eleve C
--     ('770e8400-e29b-41d4-a716-446655442101', '550e8400-e29b-41d4-a716-446655440300'), -- Eleve A
--     ('770e8400-e29b-41d4-a716-446655442101', '550e8400-e29b-41d4-a716-446655440302'); -- Eleve C


    -- Supprimer les demandes d'accès existantes pour les professeurs
    DELETE FROM ressources.demandes_acces
    WHERE utilisateur_id IN (
        SELECT professeurs_id FROM ressources.professeurs
    );

    -- Supprimer les accès directs pour les professeurs (ils doivent passer par droits de publication)
    DELETE FROM ressources.acceder
    WHERE utilisateur_id IN (
        SELECT professeurs_id FROM ressources.professeurs
    );


    -- Update existing messages to include subjects
    UPDATE ressources.messages SET objet = 'Salutation.' WHERE id = '550e8400-e29b-41d4-a716-446655440010';
    UPDATE ressources.messages SET objet = 'Réunion importante' WHERE id = '550e8400-e29b-41d4-a716-446655440011';
    UPDATE ressources.messages SET objet = 'Remerciements' WHERE id = '550e8400-e29b-41d4-a716-446655440012';
    UPDATE ressources.messages SET objet = 'Document partagé' WHERE id = '550e8400-e29b-41d4-a716-446655440013';
    UPDATE ressources.messages SET objet = 'Prochaine réunion' WHERE id = '550e8400-e29b-41d4-a716-446655440014';





    DELETE FROM ressources.acceder
    WHERE utilisateur_id = '550e8400-e29b-41d4-a716-446655440300'
      AND classe_id = '550e8400-e29b-41d4-a716-446655440401';