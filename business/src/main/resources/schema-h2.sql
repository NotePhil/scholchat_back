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