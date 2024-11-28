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
    etat        VARCHAR(255),
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
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL
);

-- Eleves table
CREATE TABLE IF NOT EXISTS ressources.eleves (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL
);

-- Classes table
CREATE TABLE IF NOT EXISTS ressources.classes (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    niveau VARCHAR(255) NOT NULL,
    date_creation TIMESTAMP,
    etat VARCHAR(50),
    etablissement_id UUID,
    CONSTRAINT fk_etablissement FOREIGN KEY (etablissement_id) REFERENCES ressources.etablissements(id)
);

-- Join table for Classes and Parents
CREATE TABLE IF NOT EXISTS ressources.classe_parents (
    classe_id UUID NOT NULL,
    parent_id UUID NOT NULL,
    PRIMARY KEY (classe_id, parent_id),
    CONSTRAINT fk_classe_parents_classe FOREIGN KEY (classe_id) REFERENCES ressources.classes(id),
    CONSTRAINT fk_classe_parents_parent FOREIGN KEY (parent_id) REFERENCES ressources.parents(id)
);

-- Join table for Classes and Eleves
CREATE TABLE IF NOT EXISTS ressources.classe_eleves (
    classe_id UUID NOT NULL,
    eleve_id UUID NOT NULL,
    PRIMARY KEY (classe_id, eleve_id),
    CONSTRAINT fk_classe_eleves_classe FOREIGN KEY (classe_id) REFERENCES ressources.classes(id),
    CONSTRAINT fk_classe_eleves_eleve FOREIGN KEY (eleve_id) REFERENCES ressources.eleves(id)
);
