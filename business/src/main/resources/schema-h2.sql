-- DROP DATABASE IF EXISTS parcours;
CREATE SCHEMA IF NOT EXISTS ressources AUTHORIZATION sa;
CREATE TABLE IF NOT EXISTS ressources.messages
(
    id               VARCHAR(255) NOT NULL,
    contenu          VARCHAR(255),
    datecreation     VARCHAR(255),
    datemodification VARCHAR(255),
    etat             VARCHAR(255),
    expediteur_id    VARCHAR(255),
    CONSTRAINT pk_messages PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS ressources.utilisateurs
(
    id          VARCHAR(255) NOT NULL,
    nom         VARCHAR(255),
    prenom      VARCHAR(255),
    email       VARCHAR(255),
    passeaccess VARCHAR(255),
    telephone   VARCHAR(255),
    adresse     VARCHAR(255),
    etat             VARCHAR(50) DEFAULT 'INACTIVE',
    activation_token VARCHAR(255) UNIQUE,
    creation_date    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_admin         BOOLEAN DEFAULT FALSE,
    CONSTRAINT pk_utilisateurs PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ressources.recevoir
(
    message_id     VARCHAR(255) NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL
);

ALTER TABLE  ressources.recevoir
    ADD CONSTRAINT IF NOT EXISTS  fk_recevoir_on_messages_entity FOREIGN KEY (message_id) REFERENCES ressources.messages (id);

ALTER TABLE ressources.recevoir
    ADD CONSTRAINT IF NOT EXISTS  fk_recevoir_on_utilisateurs_entity FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs (id);

ALTER TABLE ressources.messages
    ADD CONSTRAINT IF NOT EXISTS  FK_MESSAGES_ON_EXPEDITEUR FOREIGN KEY (expediteur_id) REFERENCES ressources.utilisateurs (id);


-- Etablissements table
CREATE TABLE IF NOT EXISTS ressources.etablissements (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL
);

-- Parents table
CREATE TABLE IF NOT EXISTS ressources.parents (
    parents_id VARCHAR(255)  PRIMARY KEY,
    CONSTRAINT fk_parents_utilisateurs FOREIGN KEY (parents_id) REFERENCES ressources.utilisateurs(id)
);

-- Eleves table
CREATE TABLE IF NOT EXISTS ressources.eleves (
    eleves_id VARCHAR(255)  PRIMARY KEY,
    niveau VARCHAR(255) NOT NULL,
    CONSTRAINT fk_eleves_utilisateurs FOREIGN KEY (eleves_id) REFERENCES ressources.utilisateurs(id)

    );

-- Classes table
CREATE TABLE IF NOT EXISTS ressources.classes (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    niveau VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP,
    code_activation VARCHAR(50),
    etat VARCHAR(50),
    etablissement_id UUID,
    CONSTRAINT fk_etablissement FOREIGN KEY (etablissement_id) REFERENCES ressources.etablissements(id)
);

-- Join table for Classes and Parents
CREATE TABLE IF NOT EXISTS ressources.classe_parents (
    classe_id UUID NOT NULL,
    parent_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (classe_id, parent_id),
    CONSTRAINT fk_classe_parents_classe FOREIGN KEY (classe_id) REFERENCES ressources.classes(id),
    CONSTRAINT fk_classe_parents_parent FOREIGN KEY (parent_id) REFERENCES ressources.parents(parents_id)
);

-- Join table for Classes and Eleves
CREATE TABLE IF NOT EXISTS ressources.classe_eleves (
    classe_id UUID NOT NULL,
    eleve_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (classe_id, eleve_id),
    CONSTRAINT fk_classe_eleves_classe FOREIGN KEY (classe_id) REFERENCES ressources.classes(id),
    CONSTRAINT fk_classe_eleves_eleve FOREIGN KEY (eleve_id) REFERENCES ressources.eleves(eleves_id)
);
-- Professeurs table (for table creation)
CREATE TABLE IF NOT EXISTS ressources.professeurs (
    professeurs_id VARCHAR(255) NOT NULL,
    cni_url_front VARCHAR(255) NOT NULL,
    cni_url_back VARCHAR(255) NOT NULL,
    nom_etablissement VARCHAR(255) ,
    matricule_professeur VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (professeurs_id),
    CONSTRAINT fk_professeurs_utilisateurs FOREIGN KEY (professeurs_id) REFERENCES ressources.utilisateurs(id)
);
-- Répétiteurs table
CREATE TABLE IF NOT EXISTS ressources.repetiteurs (
    repetiteurs_id VARCHAR(255) NOT NULL,
    cni_url_front VARCHAR(255) NOT NULL,
    cni_url_back VARCHAR(255) NOT NULL,
    photo_full_picture VARCHAR(255) NOT NULL,
    nom_classe VARCHAR(255) NOT NULL,
    PRIMARY KEY (repetiteurs_id),
    CONSTRAINT fk_repetiteurs_utilisateurs FOREIGN KEY (repetiteurs_id) REFERENCES ressources.utilisateurs(id)
);
-- Create professeurs table
CREATE TABLE IF NOT EXISTS ressources.professeurs (
    professeurs_id VARCHAR(255) NOT NULL,
    cni_url_front VARCHAR(255) NOT NULL,
    cni_url_back VARCHAR(255) NOT NULL,
    nom_etablissement VARCHAR(255) NOT NULL,
    nom_classe VARCHAR(255) NOT NULL,
    matricule_professeur VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (professeurs_id),
    CONSTRAINT fk_professeurs_utilisateurs FOREIGN KEY (professeurs_id) REFERENCES ressources.utilisateurs(id)
);

-- Create repetiteurs table
CREATE TABLE IF NOT EXISTS ressources.repetiteurs (
    repetiteurs_id VARCHAR(255) NOT NULL,
    cni_url_front VARCHAR(255) NOT NULL,
    cni_url_back VARCHAR(255) NOT NULL,
    photo_full_picture VARCHAR(255) NOT NULL,
    nom_classe VARCHAR(255) NOT NULL,
    PRIMARY KEY (repetiteurs_id),
    CONSTRAINT fk_repetiteurs_utilisateurs FOREIGN KEY (repetiteurs_id) REFERENCES ressources.utilisateurs(id)
);

-- Create the canaux table
CREATE TABLE IF NOT EXISTS ressources.canaux (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    professeur_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL,
    CONSTRAINT fk_canaux_professeurs FOREIGN KEY (professeur_id) REFERENCES ressources.professeurs(professeurs_id),
    CONSTRAINT fk_canaux_classes FOREIGN KEY (classe_id) REFERENCES ressources.classes(id)
);

-- Create the refresh_tokens table
CREATE TABLE IF NOT EXISTS ressources.refresh_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    utilisateur_id UUID NOT NULL,
    CONSTRAINT fk_refresh_tokens_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE
);





