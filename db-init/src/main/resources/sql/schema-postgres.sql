CREATE SCHEMA IF NOT EXISTS ressources;

SET search_path TO ressources;

CREATE TABLE IF NOT EXISTS utilisateurs (
    id VARCHAR(255) NOT NULL,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    passeaccess VARCHAR(255),
    telephone VARCHAR(255),
    adresse VARCHAR(255),
    etat VARCHAR(50) DEFAULT 'INACTIVE',
    activation_token VARCHAR(255) UNIQUE,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_admin BOOLEAN DEFAULT FALSE,
    CONSTRAINT pk_utilisateurs PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS etablissements (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    localisation VARCHAR(255),
    pays VARCHAR(255),
    email VARCHAR(255),
    telephone VARCHAR(255),
    option_envoi_mail_new_classe BOOLEAN DEFAULT FALSE,
    option_token_general BOOLEAN DEFAULT FALSE,
    code_unique VARCHAR(255) UNIQUE,
    gestionnaire_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS professeurs (
    professeurs_id VARCHAR(255) NOT NULL,
    cni_url_front VARCHAR(255),
    cni_url_back VARCHAR(255),
    selfie_url VARCHAR(255),
    matricule_professeur VARCHAR(255) UNIQUE,
    has_uploaded BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (professeurs_id)
);

CREATE TABLE IF NOT EXISTS classes (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    niveau VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP,
    code_activation VARCHAR(50),
    etat VARCHAR(50),
    etablissement_id VARCHAR(255),
    moderator_id VARCHAR(255),
    droit_publication VARCHAR(50),
    acces_majeur BOOLEAN DEFAULT FALSE,
    payment_required BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS parents (
    parents_id VARCHAR(255) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS eleves (
    eleves_id VARCHAR(255) PRIMARY KEY,
    niveau VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS repetiteurs (
    repetiteurs_id VARCHAR(255) NOT NULL,
    cni_url_front VARCHAR(255) NOT NULL,
    cni_url_back VARCHAR(255) NOT NULL,
    photo_full_picture VARCHAR(255) NOT NULL,
    nom_classe VARCHAR(255) NOT NULL,
    PRIMARY KEY (repetiteurs_id)
);

CREATE TABLE IF NOT EXISTS matieres (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    etat VARCHAR(50) DEFAULT 'ACTIF'
);

CREATE TABLE IF NOT EXISTS parent_eleve (
    parent_id VARCHAR(255) NOT NULL,
    eleve_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (parent_id, eleve_id),
    FOREIGN KEY (parent_id) REFERENCES parents(parents_id),
    FOREIGN KEY (eleve_id) REFERENCES eleves(eleves_id)
);

CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(255) NOT NULL,
    objet VARCHAR(255),
    contenu VARCHAR(255),
    datecreation VARCHAR(255),
    datemodification VARCHAR(255),
    etat VARCHAR(255),
    expediteur_id VARCHAR(255),
    deleted BOOLEAN DEFAULT FALSE,
    date_suppression VARCHAR(255),
    etat_original VARCHAR(255),
    CONSTRAINT pk_messages PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS evenements (
    id VARCHAR(255) PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    lieu VARCHAR(255),
    etat VARCHAR(50) NOT NULL,
    heure_debut TIMESTAMP NOT NULL,
    heure_fin TIMESTAMP,
    createur_id VARCHAR(255) NOT NULL,
    visibility VARCHAR(20) DEFAULT 'PUBLIC'
);

CREATE TABLE IF NOT EXISTS evenement_classes (
    evenement_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (evenement_id, classe_id),
    FOREIGN KEY (evenement_id) REFERENCES evenements(id) ON DELETE CASCADE,
    FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS professeur_classes_moderees (
    professeur_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (professeur_id, classe_id)
);

CREATE TABLE IF NOT EXISTS classe_parents (
    classe_id VARCHAR(255) NOT NULL,
    parent_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (classe_id, parent_id)
);

CREATE TABLE IF NOT EXISTS classe_eleves (
    classe_id VARCHAR(255) NOT NULL,
    eleve_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (classe_id, eleve_id)
);

CREATE TABLE IF NOT EXISTS canaux (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    professeur_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS professeur_matiere (
    professeur_id VARCHAR(255) NOT NULL,
    matiere_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (professeur_id, matiere_id)
);

CREATE TABLE IF NOT EXISTS classe_matieres (
    matiere_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (matiere_id, classe_id)
);

CREATE TABLE IF NOT EXISTS droit_publication (
    utilisateur_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    date_attribution TIMESTAMP NOT NULL,
    peut_publier BOOLEAN NOT NULL DEFAULT FALSE,
    peut_moderer BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (utilisateur_id, classe_id),
    CONSTRAINT fk_droit_publication_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id),
    CONSTRAINT fk_droit_publication_classe FOREIGN KEY (classe_id) REFERENCES classes(id)
);

CREATE TABLE IF NOT EXISTS recevoir (
    message_id VARCHAR(255) NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS evenement_participants (
    evenement_id VARCHAR(255) NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (evenement_id, utilisateur_id)
);

CREATE TABLE IF NOT EXISTS motifs_rejet (
    id VARCHAR(255) PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    descriptif VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS motifs_rejet_classe (
    id VARCHAR(255) PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    descriptif VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS media (
    id VARCHAR(255) PRIMARY KEY,
    bucket_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(50),
    media_type VARCHAR(50),
    owner_id VARCHAR(255),
    uploaded_date TIMESTAMP,
    evenement_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS interactions (
    id VARCHAR(255) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    niveau VARCHAR(50),
    created_by VARCHAR(255) NOT NULL,
    event_id VARCHAR(255),
    message_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS histo_activation (
    id VARCHAR(255) PRIMARY KEY,
    classe_id VARCHAR(255) NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    date_activation TIMESTAMP NOT NULL,
    date_desactivation TIMESTAMP,
    motif_desactivation VARCHAR(255),
    is_active BOOLEAN NOT NULL,
    etat_classe VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS message_classes (
    message_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (message_id, classe_id),
    CONSTRAINT fk_message_classes_message FOREIGN KEY (message_id) REFERENCES messages(id),
    CONSTRAINT fk_message_classes_classe FOREIGN KEY (classe_id) REFERENCES classes(id)
);

CREATE TABLE IF NOT EXISTS acceder (
    utilisateur_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    date_acces TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (utilisateur_id, classe_id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id),
    FOREIGN KEY (classe_id) REFERENCES classes(id)
);

CREATE TABLE IF NOT EXISTS demandes_acces (
    id VARCHAR(255) PRIMARY KEY,
    utilisateur_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    code_activation VARCHAR(255) NOT NULL,
    etat VARCHAR(50) NOT NULL,
    date_demande TIMESTAMP NOT NULL,
    date_traitement TIMESTAMP,
    motif_rejet VARCHAR(255),
    est_parent BOOLEAN NOT NULL DEFAULT FALSE,
    eleve_associe_id VARCHAR(255),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id),
    FOREIGN KEY (classe_id) REFERENCES classes(id),
    FOREIGN KEY (eleve_associe_id) REFERENCES eleves(eleves_id)
);

CREATE TABLE IF NOT EXISTS cours (
    id VARCHAR(255) PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_creation TIMESTAMP NOT NULL,
    etat VARCHAR(50) NOT NULL,
    reference TEXT,
    contenu TEXT,
    redacteur_id VARCHAR(255) NOT NULL,
    restriction VARCHAR(50) DEFAULT 'PRIVE',
    FOREIGN KEY (redacteur_id) REFERENCES professeurs(professeurs_id)
);

CREATE TABLE IF NOT EXISTS cours_matiere (
    cours_id VARCHAR(255) NOT NULL,
    matiere_id VARCHAR(255) NOT NULL,
    date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ordre_dans_cours INTEGER,
    PRIMARY KEY (cours_id, matiere_id),
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE,
    FOREIGN KEY (matiere_id) REFERENCES matieres(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS chapitres (
    id VARCHAR(255) PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    ordre INTEGER NOT NULL,
    contenu TEXT NOT NULL,
    image_url VARCHAR(255),
    cours_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cours_programmer (
    id VARCHAR(255) PRIMARY KEY,
    cours_id VARCHAR(255) NOT NULL,
    date_cours_prevue TIMESTAMP NOT NULL,
    date_debut_effectif TIMESTAMP,
    date_fin_effectif TIMESTAMP,
    etat_cours_programme VARCHAR(50) NOT NULL,
    classe_id VARCHAR(255),
    lieu VARCHAR(255) NOT NULL,
    description TEXT,
    date_creation TIMESTAMP,
    date_modification TIMESTAMP,
    cree_par VARCHAR(255),
    modifie_par VARCHAR(255),
    professeur_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (cours_id) REFERENCES cours(id),
    FOREIGN KEY (classe_id) REFERENCES classes(id),
    FOREIGN KEY (professeur_id) REFERENCES professeurs(professeurs_id)
);

CREATE TABLE IF NOT EXISTS cours_programmer_participants (
    cours_programmer_id VARCHAR(255) NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (cours_programmer_id, utilisateur_id),
    FOREIGN KEY (cours_programmer_id) REFERENCES cours_programmer(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cours_programmer_classes (
    cours_programmer_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (cours_programmer_id, classe_id),
    FOREIGN KEY (cours_programmer_id) REFERENCES cours_programmer(id) ON DELETE CASCADE,
    FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS exercises (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description TEXT,
    date_creation TIMESTAMP NOT NULL,
    etat VARCHAR(50) NOT NULL,
    restriction VARCHAR(50) NOT NULL DEFAULT 'PRIVE',
    niveau VARCHAR(50) NOT NULL,
    redacteur_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (redacteur_id) REFERENCES professeurs(professeurs_id)
);

CREATE TABLE IF NOT EXISTS cours_exercises (
    exercise_id VARCHAR(255) NOT NULL,
    cours_id VARCHAR(255) NOT NULL,
    date_liaison TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exercise_id, cours_id),
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS questions_reponses (
    id VARCHAR(255) PRIMARY KEY,
    intitule VARCHAR(1000) NOT NULL,
    reponse TEXT,
    type_question VARCHAR(50) NOT NULL,
    exercise_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS exercise_matieres (
    exercise_id VARCHAR(255) NOT NULL,
    matiere_id VARCHAR(255) NOT NULL,
    date_association TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exercise_id, matiere_id),
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    FOREIGN KEY (matiere_id) REFERENCES matieres(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS repondre (
    utilisateur_id VARCHAR(255) NOT NULL,
    question_id VARCHAR(255) NOT NULL,
    note VARCHAR(50),
    appreciation VARCHAR(255),
    reponse_utilisateur TEXT,
    date_reponse TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    est_correcte BOOLEAN,
    PRIMARY KEY (utilisateur_id, question_id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions_reponses(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS exercises_programmer (
    exercise_id VARCHAR(255) PRIMARY KEY,
    date_exo_prevue TIMESTAMP NOT NULL,
    date_debut_exo_effectif TIMESTAMP NOT NULL,
    date_fin_exo_effectif TIMESTAMP NOT NULL,
    etat_exercise_programmer VARCHAR(50),
    programme_par_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    FOREIGN KEY (programme_par_id) REFERENCES professeurs(professeurs_id)
);

CREATE TABLE IF NOT EXISTS exercise_programmer_classes (
    exercise_programmer_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    date_diffusion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exercise_programmer_id, classe_id),
    FOREIGN KEY (exercise_programmer_id) REFERENCES exercises_programmer(exercise_id) ON DELETE CASCADE,
    FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS participer_exo (
    utilisateur_id VARCHAR(255) NOT NULL,
    exercise_programmer_id VARCHAR(255) NOT NULL,
    note VARCHAR(50),
    appreciation VARCHAR(255),
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP,
    date_soumission TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (utilisateur_id, exercise_programmer_id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_programmer_id) REFERENCES exercises_programmer(exercise_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    actor_id VARCHAR(255),
    actor_name VARCHAR(255),
    related_entity_id VARCHAR(255),
    related_entity_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

-- Add constraints
ALTER TABLE professeurs
    ADD CONSTRAINT fk_professeurs_utilisateurs
    FOREIGN KEY (professeurs_id) REFERENCES utilisateurs(id);

ALTER TABLE parents
    ADD CONSTRAINT fk_parents_utilisateurs
    FOREIGN KEY (parents_id) REFERENCES utilisateurs(id);

ALTER TABLE eleves
    ADD CONSTRAINT fk_eleves_utilisateurs
    FOREIGN KEY (eleves_id) REFERENCES utilisateurs(id);

ALTER TABLE repetiteurs
    ADD CONSTRAINT fk_repetiteurs_utilisateurs
    FOREIGN KEY (repetiteurs_id) REFERENCES utilisateurs(id);

ALTER TABLE classes
    ADD CONSTRAINT fk_etablissement
    FOREIGN KEY (etablissement_id) REFERENCES etablissements(id);

ALTER TABLE etablissements
    ADD CONSTRAINT fk_etablissement_gestionnaire
    FOREIGN KEY (gestionnaire_id) REFERENCES utilisateurs(id);

ALTER TABLE classes
    ADD CONSTRAINT fk_classes_moderator
    FOREIGN KEY (moderator_id) REFERENCES professeurs(professeurs_id);

ALTER TABLE professeur_classes_moderees
    ADD CONSTRAINT fk_prof_moderateur
    FOREIGN KEY (professeur_id) REFERENCES professeurs(professeurs_id);

ALTER TABLE professeur_classes_moderees
    ADD CONSTRAINT fk_classe_moderee
    FOREIGN KEY (classe_id) REFERENCES classes(id);

ALTER TABLE classe_parents
    ADD CONSTRAINT fk_classe_parents_classe
    FOREIGN KEY (classe_id) REFERENCES classes(id);

ALTER TABLE classe_parents
    ADD CONSTRAINT fk_classe_parents_parent
    FOREIGN KEY (parent_id) REFERENCES parents(parents_id);

ALTER TABLE classe_eleves
    ADD CONSTRAINT fk_classe_eleves_classe
    FOREIGN KEY (classe_id) REFERENCES classes(id);

ALTER TABLE classe_eleves
    ADD CONSTRAINT fk_classe_eleves_eleve
    FOREIGN KEY (eleve_id) REFERENCES eleves(eleves_id);

ALTER TABLE canaux
    ADD CONSTRAINT fk_canaux_professeurs
    FOREIGN KEY (professeur_id) REFERENCES professeurs(professeurs_id);

ALTER TABLE canaux
    ADD CONSTRAINT fk_canaux_classes
    FOREIGN KEY (classe_id) REFERENCES classes(id);

ALTER TABLE professeur_matiere
    ADD CONSTRAINT fk_professeur_matiere_professeur
    FOREIGN KEY (professeur_id) REFERENCES professeurs(professeurs_id);

ALTER TABLE professeur_matiere
    ADD CONSTRAINT fk_professeur_matiere_matiere
    FOREIGN KEY (matiere_id) REFERENCES matieres(id);

ALTER TABLE classe_matieres
    ADD CONSTRAINT fk_classe_matieres_matiere
    FOREIGN KEY (matiere_id) REFERENCES matieres(id);

ALTER TABLE classe_matieres
    ADD CONSTRAINT fk_classe_matieres_classe
    FOREIGN KEY (classe_id) REFERENCES classes(id);

ALTER TABLE evenements
    ADD CONSTRAINT fk_evenement_professeur
    FOREIGN KEY (createur_id) REFERENCES professeurs(professeurs_id);

ALTER TABLE evenement_participants
    ADD CONSTRAINT fk_evenement_participants_evenement
    FOREIGN KEY (evenement_id) REFERENCES evenements(id);

ALTER TABLE evenement_participants
    ADD CONSTRAINT fk_evenement_participants_utilisateur
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id);

ALTER TABLE messages
    ADD CONSTRAINT FK_MESSAGES_ON_EXPEDITEUR
    FOREIGN KEY (expediteur_id) REFERENCES utilisateurs(id);

ALTER TABLE recevoir
    ADD CONSTRAINT fk_recevoir_on_messages_entity
    FOREIGN KEY (message_id) REFERENCES messages(id);

ALTER TABLE recevoir
    ADD CONSTRAINT fk_recevoir_on_utilisateurs_entity
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id);

ALTER TABLE media
    ADD CONSTRAINT fk_media_owner
    FOREIGN KEY (owner_id) REFERENCES utilisateurs(id) ON DELETE SET NULL;

ALTER TABLE media
    ADD CONSTRAINT fk_media_evenement
    FOREIGN KEY (evenement_id) REFERENCES evenements(id);

ALTER TABLE interactions
    ADD CONSTRAINT fk_interaction_user
    FOREIGN KEY (created_by) REFERENCES utilisateurs(id);

ALTER TABLE interactions
    ADD CONSTRAINT fk_interaction_event
    FOREIGN KEY (event_id) REFERENCES evenements(id);

ALTER TABLE interactions
    ADD CONSTRAINT fk_interaction_message
    FOREIGN KEY (message_id) REFERENCES messages(id);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_utilisateur
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_classes_etat ON classes(etat);
CREATE INDEX IF NOT EXISTS idx_classes_moderator ON classes(moderator_id);
CREATE INDEX IF NOT EXISTS idx_prof_moderated_classes ON professeur_classes_moderees(professeur_id);
CREATE INDEX IF NOT EXISTS idx_classe_etablissement ON classes(etablissement_id);
CREATE INDEX IF NOT EXISTS idx_utilisateur_email ON utilisateurs(email);
CREATE INDEX IF NOT EXISTS idx_acceder_classe ON acceder(classe_id);
CREATE INDEX IF NOT EXISTS idx_acceder_utilisateur ON acceder(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_chapitres_cours ON chapitres(cours_id);
CREATE INDEX IF NOT EXISTS idx_chapitres_ordre ON chapitres(ordre);
CREATE INDEX IF NOT EXISTS idx_cours_restriction ON cours(restriction);
CREATE INDEX IF NOT EXISTS idx_exercises_redacteur ON exercises(redacteur_id);
CREATE INDEX IF NOT EXISTS idx_exercises_niveau ON exercises(niveau);
CREATE INDEX IF NOT EXISTS idx_exercises_restriction ON exercises(restriction);
CREATE INDEX IF NOT EXISTS idx_cours_exercises_cours ON cours_exercises(cours_id);
CREATE INDEX IF NOT EXISTS idx_repondre_utilisateur ON repondre(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_repondre_question ON repondre(question_id);
CREATE INDEX IF NOT EXISTS idx_repondre_date ON repondre(date_reponse);
CREATE INDEX IF NOT EXISTS idx_exercise_programmer_prof ON exercises_programmer(programme_par_id);
CREATE INDEX IF NOT EXISTS idx_exercise_programmer_date_prevue ON exercises_programmer(date_exo_prevue);
CREATE INDEX IF NOT EXISTS idx_exercise_programmer_etat ON exercises_programmer(etat_exercise_programmer);
CREATE INDEX IF NOT EXISTS idx_exercise_programmer_classes_classe ON exercise_programmer_classes(classe_id);
CREATE INDEX IF NOT EXISTS idx_participer_exo_utilisateur ON participer_exo(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_participer_exo_exercise ON participer_exo(exercise_programmer_id);
CREATE INDEX IF NOT EXISTS idx_participer_exo_dates ON participer_exo(date_debut, date_fin);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications(user_id, is_read) WHERE is_read = FALSE;