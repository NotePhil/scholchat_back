-- DROP DATABASE IF EXISTS parcours;
CREATE SCHEMA IF NOT EXISTS ressources AUTHORIZATION sa;

-- Disable foreign key checks temporarily
SET REFERENTIAL_INTEGRITY FALSE;

-- 1. Create all base tables without foreign keys first
CREATE TABLE IF NOT EXISTS ressources.utilisateurs (
    id VARCHAR(255) NOT NULL,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    email VARCHAR(255),
    passeaccess VARCHAR(255),
    telephone VARCHAR(255),
    adresse VARCHAR(255),
    etat VARCHAR(50) DEFAULT 'INACTIVE',
    activation_token VARCHAR(255) UNIQUE,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_admin BOOLEAN DEFAULT FALSE,
    CONSTRAINT pk_utilisateurs PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ressources.etablissements (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    localisation VARCHAR(255),
    pays VARCHAR(255),
    email VARCHAR(255),
    telephone VARCHAR(255),
    option_envoi_mail_classe BOOLEAN DEFAULT FALSE,
    option_token_general BOOLEAN DEFAULT FALSE,
    code_unique BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS ressources.professeurs (
    professeurs_id VARCHAR(255) NOT NULL,
    cni_url_front VARCHAR(255) NOT NULL,
    cni_url_back VARCHAR(255) NOT NULL,
    selfie_url VARCHAR(255),
    matricule_professeur VARCHAR(255) UNIQUE,
    PRIMARY KEY (professeurs_id)
);

CREATE TABLE IF NOT EXISTS ressources.classes (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    niveau VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP,
    code_activation VARCHAR(50),
    etat VARCHAR(50),
    etablissement_id UUID,
    moderator_id VARCHAR(255)
);

-- 2. Create other tables that depend on the base tables
CREATE TABLE IF NOT EXISTS ressources.parents (
    parents_id VARCHAR(255) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS ressources.eleves (
    eleves_id VARCHAR(255) PRIMARY KEY,
    niveau VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS ressources.repetiteurs (
    repetiteurs_id VARCHAR(255) NOT NULL,
    cni_url_front VARCHAR(255) NOT NULL,
    cni_url_back VARCHAR(255) NOT NULL,
    photo_full_picture VARCHAR(255) NOT NULL,
    nom_classe VARCHAR(255) NOT NULL,
    PRIMARY KEY (repetiteurs_id)
);

CREATE TABLE IF NOT EXISTS ressources.matieres (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    etat VARCHAR(50) DEFAULT 'ACTIF'
);

CREATE TABLE IF NOT EXISTS ressources.messages (
    id VARCHAR(255) NOT NULL,
    contenu VARCHAR(255),
    datecreation VARCHAR(255),
    datemodification VARCHAR(255),
    etat VARCHAR(255),
    expediteur_id VARCHAR(255),
    CONSTRAINT pk_messages PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ressources.evenements (
    id UUID PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    lieu VARCHAR(255),
    etat VARCHAR(50) NOT NULL,
    heure_debut TIMESTAMP NOT NULL,
    heure_fin TIMESTAMP,
    createur_id VARCHAR(255) NOT NULL
);

-- 3. Create join tables
CREATE TABLE IF NOT EXISTS ressources.professeur_classes_moderees (
    professeur_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL,
    PRIMARY KEY (professeur_id, classe_id)
);

CREATE TABLE IF NOT EXISTS ressources.classe_parents (
    classe_id UUID NOT NULL,
    parent_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (classe_id, parent_id)
);

CREATE TABLE IF NOT EXISTS ressources.classe_eleves (
    classe_id UUID NOT NULL,
    eleve_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (classe_id, eleve_id)
);

CREATE TABLE IF NOT EXISTS ressources.canaux (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    professeur_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS ressources.professeur_matiere (
    professeur_id VARCHAR(255) NOT NULL,
    matiere_id UUID NOT NULL,
    PRIMARY KEY (professeur_id, matiere_id)
);

CREATE TABLE IF NOT EXISTS ressources.classe_matieres (
    matiere_id UUID NOT NULL,
    classe_id UUID NOT NULL,
    PRIMARY KEY (matiere_id, classe_id)
);

CREATE TABLE IF NOT EXISTS ressources.recevoir (
    message_id VARCHAR(255) NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS ressources.evenement_participants (
    evenement_id UUID NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (evenement_id, utilisateur_id)
);

CREATE TABLE IF NOT EXISTS ressources.motifs_rejet (
    id VARCHAR(255) PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    descriptif VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS ressources.motifs_rejet_classe (
    id VARCHAR(255) PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    descriptif VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS ressources.media (
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
    evenement_id UUID
);

CREATE TABLE IF NOT EXISTS ressources.interactions (
    id VARCHAR(255) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    niveau VARCHAR(50),
    created_by VARCHAR(255) NOT NULL,
    event_id VARCHAR(255),
    message_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS ressources.refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    utilisateur_id UUID NOT NULL
);

-- Re-enable foreign key checks
SET REFERENTIAL_INTEGRITY TRUE;

-- 4. Add all foreign key constraints after all tables exist
ALTER TABLE ressources.professeurs
    ADD CONSTRAINT fk_professeurs_utilisateurs
    FOREIGN KEY (professeurs_id) REFERENCES ressources.utilisateurs(id);

ALTER TABLE ressources.parents
    ADD CONSTRAINT fk_parents_utilisateurs
    FOREIGN KEY (parents_id) REFERENCES ressources.utilisateurs(id);

ALTER TABLE ressources.eleves
    ADD CONSTRAINT fk_eleves_utilisateurs
    FOREIGN KEY (eleves_id) REFERENCES ressources.utilisateurs(id);

ALTER TABLE ressources.repetiteurs
    ADD CONSTRAINT fk_repetiteurs_utilisateurs
    FOREIGN KEY (repetiteurs_id) REFERENCES ressources.utilisateurs(id);

ALTER TABLE ressources.classes
    ADD CONSTRAINT fk_etablissement
    FOREIGN KEY (etablissement_id) REFERENCES ressources.etablissements(id);

ALTER TABLE ressources.classes
    ADD CONSTRAINT fk_classes_moderator
    FOREIGN KEY (moderator_id) REFERENCES ressources.professeurs(professeurs_id);

ALTER TABLE ressources.professeur_classes_moderees
    ADD CONSTRAINT fk_prof_moderateur
    FOREIGN KEY (professeur_id) REFERENCES ressources.professeurs(professeurs_id);

ALTER TABLE ressources.professeur_classes_moderees
    ADD CONSTRAINT fk_classe_moderee
    FOREIGN KEY (classe_id) REFERENCES ressources.classes(id);

ALTER TABLE ressources.classe_parents
    ADD CONSTRAINT fk_classe_parents_classe
    FOREIGN KEY (classe_id) REFERENCES ressources.classes(id);

ALTER TABLE ressources.classe_parents
    ADD CONSTRAINT fk_classe_parents_parent
    FOREIGN KEY (parent_id) REFERENCES ressources.parents(parents_id);

ALTER TABLE ressources.classe_eleves
    ADD CONSTRAINT fk_classe_eleves_classe
    FOREIGN KEY (classe_id) REFERENCES ressources.classes(id);

ALTER TABLE ressources.classe_eleves
    ADD CONSTRAINT fk_classe_eleves_eleve
    FOREIGN KEY (eleve_id) REFERENCES ressources.eleves(eleves_id);

ALTER TABLE ressources.canaux
    ADD CONSTRAINT fk_canaux_professeurs
    FOREIGN KEY (professeur_id) REFERENCES ressources.professeurs(professeurs_id);

ALTER TABLE ressources.canaux
    ADD CONSTRAINT fk_canaux_classes
    FOREIGN KEY (classe_id) REFERENCES ressources.classes(id);

ALTER TABLE ressources.professeur_matiere
    ADD CONSTRAINT fk_professeur_matiere_professeur
    FOREIGN KEY (professeur_id) REFERENCES ressources.professeurs(professeurs_id);

ALTER TABLE ressources.professeur_matiere
    ADD CONSTRAINT fk_professeur_matiere_matiere
    FOREIGN KEY (matiere_id) REFERENCES ressources.matieres(id);

ALTER TABLE ressources.classe_matieres
    ADD CONSTRAINT fk_classe_matieres_matiere
    FOREIGN KEY (matiere_id) REFERENCES ressources.matieres(id);

ALTER TABLE ressources.classe_matieres
    ADD CONSTRAINT fk_classe_matieres_classe
    FOREIGN KEY (classe_id) REFERENCES ressources.classes(id);

ALTER TABLE ressources.evenements
    ADD CONSTRAINT fk_evenement_professeur
    FOREIGN KEY (createur_id) REFERENCES ressources.professeurs(professeurs_id);

ALTER TABLE ressources.evenement_participants
    ADD CONSTRAINT fk_evenement_participants_evenement
    FOREIGN KEY (evenement_id) REFERENCES ressources.evenements(id);

ALTER TABLE ressources.evenement_participants
    ADD CONSTRAINT fk_evenement_participants_utilisateur
    FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs(id);

ALTER TABLE ressources.messages
    ADD CONSTRAINT FK_MESSAGES_ON_EXPEDITEUR
    FOREIGN KEY (expediteur_id) REFERENCES ressources.utilisateurs(id);

ALTER TABLE ressources.recevoir
    ADD CONSTRAINT fk_recevoir_on_messages_entity
    FOREIGN KEY (message_id) REFERENCES ressources.messages(id);

ALTER TABLE ressources.recevoir
    ADD CONSTRAINT fk_recevoir_on_utilisateurs_entity
    FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs(id);

ALTER TABLE ressources.media
    ADD CONSTRAINT fk_media_owner
    FOREIGN KEY (owner_id) REFERENCES ressources.utilisateurs(id) ON DELETE SET NULL;

ALTER TABLE ressources.media
    ADD CONSTRAINT fk_media_evenement
    FOREIGN KEY (evenement_id) REFERENCES ressources.evenements(id);

ALTER TABLE ressources.interactions
    ADD CONSTRAINT fk_interaction_user
    FOREIGN KEY (created_by) REFERENCES ressources.utilisateurs(id);

ALTER TABLE ressources.interactions
    ADD CONSTRAINT fk_interaction_event
    FOREIGN KEY (event_id) REFERENCES ressources.evenements(id);

ALTER TABLE ressources.interactions
    ADD CONSTRAINT fk_interaction_message
    FOREIGN KEY (message_id) REFERENCES ressources.messages(id);

ALTER TABLE ressources.refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_utilisateur
    FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_classes_moderator ON ressources.classes(moderator_id);
CREATE INDEX IF NOT EXISTS idx_prof_moderated_classes ON ressources.professeur_classes_moderees(professeur_id);
CREATE INDEX IF NOT EXISTS idx_classe_etablissement ON ressources.classes(etablissement_id);
CREATE INDEX IF NOT EXISTS idx_utilisateur_email ON ressources.utilisateurs(email);