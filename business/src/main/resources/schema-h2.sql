DROP DATABASE IF EXISTS parcours;

CREATE SCHEMA IF NOT EXISTS ressources AUTHORIZATION sa;

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
CREATE TABLE IF NOT EXISTS ressources.professeurs
(
    id                     VARCHAR(255) NOT NULL,
    cni_url_front          VARCHAR(255),
    cni_url_back           VARCHAR(255),
    nom_etablissement      VARCHAR(255),
    nom_classe             VARCHAR(255),
    matricule_professeur   VARCHAR(255) UNIQUE,
    CONSTRAINT pk_professeurs PRIMARY KEY (id),
    CONSTRAINT fk_professeurs_on_utilisateurs FOREIGN KEY (id) REFERENCES ressources.utilisateurs (id)
);
CREATE TABLE IF NOT EXISTS ressources.repetiteurs
(
    id                VARCHAR(255) NOT NULL,
    cni_url_front     VARCHAR(255),
    cni_url_back      VARCHAR(255),
    piece_identite    VARCHAR(255),
    photo             VARCHAR(255),
    CONSTRAINT pk_repetiteurs PRIMARY KEY (id),
    CONSTRAINT fk_repetiteurs_on_utilisateurs FOREIGN KEY (id) REFERENCES ressources.utilisateurs (id)
);

-- Table for Messages
CREATE TABLE IF NOT EXISTS ressources.messages
(
    id               VARCHAR(255) NOT NULL,
    contenu          VARCHAR(255),
    datecreation     VARCHAR(255),
    datemodification VARCHAR(255),
    etat             VARCHAR(255),
    expediteur_id    VARCHAR(255),
    CONSTRAINT pk_messages PRIMARY KEY (id),
    CONSTRAINT fk_messages_on_expediteur FOREIGN KEY (expediteur_id) REFERENCES ressources.utilisateurs (id)
);

-- Table for Recevoir
CREATE TABLE IF NOT EXISTS ressources.recevoir
(
    message_id     VARCHAR(255) NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_recevoir PRIMARY KEY (message_id, utilisateur_id),
    CONSTRAINT fk_recevoir_on_messages FOREIGN KEY (message_id) REFERENCES ressources.messages (id),
    CONSTRAINT fk_recevoir_on_utilisateurs FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs (id)
);
