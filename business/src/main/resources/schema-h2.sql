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
    option_envoi_mail_new_classe BOOLEAN DEFAULT FALSE,
    option_token_general BOOLEAN DEFAULT FALSE,
    code_unique VARCHAR(255) UNIQUE,
    gestionnaire_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS ressources.professeurs (
    professeurs_id VARCHAR(255) NOT NULL,
    cni_url_front VARCHAR(255) ,
    cni_url_back VARCHAR(255) ,
    selfie_url VARCHAR(255),
    matricule_professeur VARCHAR(255) UNIQUE,
    has_uploaded BOOLEAN DEFAULT FALSE,
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
    moderator_id VARCHAR(255),
    droit_publication VARCHAR(50),
    acces_majeur BOOLEAN DEFAULT FALSE,
    payment_required BOOLEAN DEFAULT FALSE
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
CREATE TABLE IF NOT EXISTS ressources.parent_eleve (
                                                       parent_id VARCHAR(255) NOT NULL,
    eleve_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (parent_id, eleve_id),
    FOREIGN KEY (parent_id) REFERENCES ressources.parents(parents_id),
    FOREIGN KEY (eleve_id) REFERENCES ressources.eleves(eleves_id)
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
CREATE TABLE IF NOT EXISTS ressources.droit_publication (
    utilisateur_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL,
    date_attribution TIMESTAMP NOT NULL,
    peut_publier BOOLEAN NOT NULL DEFAULT FALSE,
    peut_moderer BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (utilisateur_id, classe_id),
    CONSTRAINT fk_droit_publication_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs(id),
    CONSTRAINT fk_droit_publication_classe FOREIGN KEY (classe_id) REFERENCES ressources.classes(id)
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

CREATE TABLE IF NOT EXISTS ressources.histo_activation (
    id UUID PRIMARY KEY,
    classe_id UUID NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    date_activation TIMESTAMP NOT NULL,
    date_desactivation TIMESTAMP,
    motif_desactivation VARCHAR(255),
    is_active BOOLEAN NOT NULL,
    etat_classe VARCHAR(50)
);


CREATE TABLE IF NOT EXISTS ressources.message_classes (
                                                          message_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL,
    PRIMARY KEY (message_id, classe_id),
    CONSTRAINT fk_message_classes_message FOREIGN KEY (message_id) REFERENCES ressources.messages(id),
    CONSTRAINT fk_message_classes_classe FOREIGN KEY (classe_id) REFERENCES ressources.classes(id)
    );
CREATE TABLE IF NOT EXISTS ressources.acceder (
                                                  utilisateur_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    date_acces TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (utilisateur_id, classe_id),
    FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs(id),
    FOREIGN KEY (classe_id) REFERENCES ressources.classes(id)
    );

CREATE TABLE IF NOT EXISTS ressources.demandes_acces (
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
    FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs(id),
    FOREIGN KEY (classe_id) REFERENCES ressources.classes(id),
    FOREIGN KEY (eleve_associe_id) REFERENCES ressources.eleves(eleves_id)
    );
-- Table pour les cours
CREATE TABLE IF NOT EXISTS ressources.cours (
                                                id UUID PRIMARY KEY,
                                                titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_creation TIMESTAMP NOT NULL,
    etat VARCHAR(50) NOT NULL,
    references TEXT,
    contenu TEXT NOT NULL,
    redacteur_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (redacteur_id) REFERENCES ressources.professeurs(professeurs_id)
    );
CREATE TABLE IF NOT EXISTS ressources.cours_matiere (
                                                        cours_id UUID NOT NULL,
                                                        matiere_id UUID NOT NULL,
                                                        date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                        ordre_dans_cours INTEGER,
                                                        PRIMARY KEY (cours_id, matiere_id),
    FOREIGN KEY (cours_id) REFERENCES ressources.cours(id) ON DELETE CASCADE,
    FOREIGN KEY (matiere_id) REFERENCES ressources.matieres(id) ON DELETE CASCADE
    );
CREATE TABLE IF NOT EXISTS ressources.chapitres (
                                                    id UUID PRIMARY KEY,
                                                    titre VARCHAR(255) NOT NULL,
    description TEXT,
    ordre INTEGER NOT NULL,
    contenu TEXT NOT NULL,
    image_url VARCHAR(255),
    cours_id UUID NOT NULL,
    FOREIGN KEY (cours_id) REFERENCES ressources.cours(id) ON DELETE CASCADE
    );
-- Table de jointure pour la relation many-to-many entre cours et matieres



-- Table pour les cours programmés
CREATE TABLE IF NOT EXISTS ressources.cours_programmer (
    id VARCHAR(255) PRIMARY KEY,
    cours_id UUID NOT NULL,
    date_cours_prevue TIMESTAMP NOT NULL,
    date_debut_effectif TIMESTAMP NOT NULL,
    date_fin_effectif TIMESTAMP NOT NULL,
    etat_cours_programme VARCHAR(50) NOT NULL,
    classe_id UUID,
    lieu VARCHAR(255) NOT NULL,
    description TEXT,

    date_creation TIMESTAMP,
    date_modification TIMESTAMP,
    cree_par VARCHAR(255),
    modifie_par VARCHAR(255),
    FOREIGN KEY (cours_id) REFERENCES ressources.cours(id),
    FOREIGN KEY (classe_id) REFERENCES ressources.classes(id)
);
CREATE TABLE IF NOT EXISTS ressources.cours_programmer_participants (
                                                                        cours_programmer_id VARCHAR(255) NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (cours_programmer_id, utilisateur_id),
    FOREIGN KEY (cours_programmer_id) REFERENCES ressources.cours_programmer(id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE
    );
-- Table de jointure pour la participation aux cours
CREATE TABLE IF NOT EXISTS ressources.evenement_participants (
    evenement_id UUID NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (evenement_id, utilisateur_id)
);
CREATE TABLE IF NOT EXISTS ressources.cours_programmer_classes (
                                                                   cours_programmer_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL,
    PRIMARY KEY (cours_programmer_id, classe_id),
    FOREIGN KEY (cours_programmer_id) REFERENCES ressources.cours_programmer(id) ON DELETE CASCADE,
    FOREIGN KEY (classe_id) REFERENCES ressources.classes(id) ON DELETE CASCADE
    );


CREATE TABLE IF NOT EXISTS ressources.exercises (
                                                    id UUID PRIMARY KEY,
                                                    nom VARCHAR(255) NOT NULL,
    description TEXT,
    date_creation TIMESTAMP NOT NULL,
    etat VARCHAR(50) NOT NULL,
    restriction VARCHAR(50) NOT NULL DEFAULT 'PRIVE',
    niveau VARCHAR(50) NOT NULL,
    redacteur_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (redacteur_id) REFERENCES ressources.professeurs(professeurs_id)
    );



CREATE TABLE IF NOT EXISTS ressources.cours_exercises (
                                                          exercise_id UUID NOT NULL,
                                                          cours_id UUID NOT NULL,
                                                          date_liaison TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                          PRIMARY KEY (exercise_id, cours_id),
    FOREIGN KEY (exercise_id) REFERENCES ressources.exercises(id) ON DELETE CASCADE,
    FOREIGN KEY (cours_id) REFERENCES ressources.cours(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS ressources.questions_reponses (
                                                             id UUID PRIMARY KEY,
                                                             intitule VARCHAR(1000) NOT NULL,
    reponse TEXT,
    type_question VARCHAR(50) NOT NULL,
    exercise_id UUID NOT NULL,
    FOREIGN KEY (exercise_id) REFERENCES ressources.exercises(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS ressources.exercise_matieres (
                                                            exercise_id UUID NOT NULL,
                                                            matiere_id UUID NOT NULL,
                                                            date_association TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                            PRIMARY KEY (exercise_id, matiere_id),
    FOREIGN KEY (exercise_id) REFERENCES ressources.exercises(id) ON DELETE CASCADE,
    FOREIGN KEY (matiere_id) REFERENCES ressources.matieres(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS ressources.repondre (
                                                   utilisateur_id VARCHAR(255) NOT NULL,
    question_id UUID NOT NULL,
    note VARCHAR(50),
    appreciation VARCHAR(255),
    reponse_utilisateur TEXT,
    date_reponse TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    est_correcte BOOLEAN,
    PRIMARY KEY (utilisateur_id, question_id),
    FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES ressources.questions_reponses(id) ON DELETE CASCADE
    );
CREATE TABLE IF NOT EXISTS ressources.exercises_programmer (
                                                               exercise_id UUID PRIMARY KEY,
                                                               date_exo_prevue TIMESTAMP NOT NULL,
                                                               date_debut_exo_effectif TIMESTAMP NOT NULL,
                                                               date_fin_exo_effectif TIMESTAMP NOT NULL,
                                                               etat_exercise_programmer VARCHAR(50),
    programme_par_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (exercise_id) REFERENCES ressources.exercises(id) ON DELETE CASCADE,
    FOREIGN KEY (programme_par_id) REFERENCES ressources.professeurs(professeurs_id)
    );

CREATE TABLE IF NOT EXISTS ressources.exercise_programmer_classes (
                                                                      exercise_programmer_id UUID NOT NULL,
                                                                      classe_id UUID NOT NULL,
                                                                      date_diffusion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                                      PRIMARY KEY (exercise_programmer_id, classe_id),
    FOREIGN KEY (exercise_programmer_id) REFERENCES ressources.exercises_programmer(exercise_id) ON DELETE CASCADE,
    FOREIGN KEY (classe_id) REFERENCES ressources.classes(id) ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS ressources.participer_exo (
                                                         utilisateur_id VARCHAR(255) NOT NULL,
    exercise_programmer_id UUID NOT NULL,
    note VARCHAR(50),
    appreciation VARCHAR(255),
    date_debut TIMESTAMP NOT NULL,
    date_fin TIMESTAMP,
    date_soumission TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (utilisateur_id, exercise_programmer_id),
    FOREIGN KEY (utilisateur_id) REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (exercise_programmer_id) REFERENCES ressources.exercises_programmer(exercise_id) ON DELETE CASCADE
    );
-- Re-enable foreign key checks
SET REFERENTIAL_INTEGRITY TRUE;

-- 4. Add all foreign key constraints after all tables exist
-- First drop constraints if they exist to avoid duplicates
ALTER TABLE ressources.classes DROP CONSTRAINT IF EXISTS fk_etablissement;
ALTER TABLE ressources.classes DROP CONSTRAINT IF EXISTS fk_classes_moderator;
ALTER TABLE ressources.professeurs DROP CONSTRAINT IF EXISTS fk_professeurs_utilisateurs;
ALTER TABLE ressources.parents DROP CONSTRAINT IF EXISTS fk_parents_utilisateurs;
ALTER TABLE ressources.eleves DROP CONSTRAINT IF EXISTS fk_eleves_utilisateurs;
ALTER TABLE ressources.repetiteurs DROP CONSTRAINT IF EXISTS fk_repetiteurs_utilisateurs;
ALTER TABLE ressources.professeur_classes_moderees DROP CONSTRAINT IF EXISTS fk_prof_moderateur;
ALTER TABLE ressources.professeur_classes_moderees DROP CONSTRAINT IF EXISTS fk_classe_moderee;
ALTER TABLE ressources.classe_parents DROP CONSTRAINT IF EXISTS fk_classe_parents_classe;
ALTER TABLE ressources.classe_parents DROP CONSTRAINT IF EXISTS fk_classe_parents_parent;
ALTER TABLE ressources.classe_eleves DROP CONSTRAINT IF EXISTS fk_classe_eleves_classe;
ALTER TABLE ressources.classe_eleves DROP CONSTRAINT IF EXISTS fk_classe_eleves_eleve;
ALTER TABLE ressources.canaux DROP CONSTRAINT IF EXISTS fk_canaux_professeurs;
ALTER TABLE ressources.canaux DROP CONSTRAINT IF EXISTS fk_canaux_classes;
ALTER TABLE ressources.professeur_matiere DROP CONSTRAINT IF EXISTS fk_professeur_matiere_professeur;
ALTER TABLE ressources.professeur_matiere DROP CONSTRAINT IF EXISTS fk_professeur_matiere_matiere;
ALTER TABLE ressources.classe_matieres DROP CONSTRAINT IF EXISTS fk_classe_matieres_matiere;
ALTER TABLE ressources.classe_matieres DROP CONSTRAINT IF EXISTS fk_classe_matieres_classe;
ALTER TABLE ressources.evenements DROP CONSTRAINT IF EXISTS fk_evenement_professeur;
ALTER TABLE ressources.evenement_participants DROP CONSTRAINT IF EXISTS fk_evenement_participants_evenement;
ALTER TABLE ressources.evenement_participants DROP CONSTRAINT IF EXISTS fk_evenement_participants_utilisateur;
ALTER TABLE ressources.messages DROP CONSTRAINT IF EXISTS FK_MESSAGES_ON_EXPEDITEUR;
ALTER TABLE ressources.recevoir DROP CONSTRAINT IF EXISTS fk_recevoir_on_messages_entity;
ALTER TABLE ressources.recevoir DROP CONSTRAINT IF EXISTS fk_recevoir_on_utilisateurs_entity;
ALTER TABLE ressources.media DROP CONSTRAINT IF EXISTS fk_media_owner;
ALTER TABLE ressources.media DROP CONSTRAINT IF EXISTS fk_media_evenement;
ALTER TABLE ressources.interactions DROP CONSTRAINT IF EXISTS fk_interaction_user;
ALTER TABLE ressources.interactions DROP CONSTRAINT IF EXISTS fk_interaction_event;
ALTER TABLE ressources.interactions DROP CONSTRAINT IF EXISTS fk_interaction_message;
ALTER TABLE ressources.refresh_tokens DROP CONSTRAINT IF EXISTS fk_refresh_tokens_utilisateur;
ALTER TABLE ressources.cours ADD COLUMN IF NOT EXISTS restriction VARCHAR(50) DEFAULT 'PRIVE';

-- Now add the constraints
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

-- Make establishment optional by allowing NULL values
ALTER TABLE ressources.classes
    ADD CONSTRAINT fk_etablissement
    FOREIGN KEY (etablissement_id) REFERENCES ressources.etablissements(id);

ALTER TABLE ressources.etablissements
    ADD CONSTRAINT fk_etablissement_gestionnaire
    FOREIGN KEY (gestionnaire_id) REFERENCES ressources.utilisateurs(id);

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

ALTER TABLE ressources.acceder
    ADD CONSTRAINT fk_acceder_classe
        FOREIGN KEY (classe_id) REFERENCES ressources.classes(id);


ALTER TABLE ressources.messages ADD COLUMN IF NOT EXISTS objet VARCHAR(255);
ALTER TABLE ressources.cours_programmer
    ADD COLUMN professeur_id VARCHAR(255) NOT NULL;

ALTER TABLE ressources.cours_programmer
    ADD CONSTRAINT fk_cours_programmer_professeur
        FOREIGN KEY (professeur_id) REFERENCES ressources.professeurs(professeurs_id);
-- Create indexes (only once)
CREATE INDEX IF NOT EXISTS idx_classes_etat ON ressources.classes(etat);
CREATE INDEX IF NOT EXISTS idx_classes_moderator ON ressources.classes(moderator_id);
CREATE INDEX IF NOT EXISTS idx_prof_moderated_classes ON ressources.professeur_classes_moderees(professeur_id);
CREATE INDEX IF NOT EXISTS idx_classe_etablissement ON ressources.classes(etablissement_id);
CREATE INDEX IF NOT EXISTS idx_utilisateur_email ON ressources.utilisateurs(email);
CREATE INDEX IF NOT EXISTS idx_acceder_classe ON ressources.acceder(classe_id);
CREATE INDEX IF NOT EXISTS idx_acceder_utilisateur ON ressources.acceder(utilisateur_id);
-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_chapitres_cours ON ressources.chapitres(cours_id);
CREATE INDEX IF NOT EXISTS idx_chapitres_ordre ON ressources.chapitres(ordre);
CREATE INDEX IF NOT EXISTS idx_cours_restriction ON ressources.cours(restriction);

ALTER TABLE ressources.cours ALTER COLUMN contenu DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_exercises_redacteur ON ressources.exercises(redacteur_id);
CREATE INDEX IF NOT EXISTS idx_exercises_niveau ON ressources.exercises(niveau);
CREATE INDEX IF NOT EXISTS idx_exercises_restriction ON ressources.exercises(restriction);
CREATE INDEX IF NOT EXISTS idx_cours_exercises_cours ON ressources.cours_exercises(cours_id);
CREATE INDEX IF NOT EXISTS idx_repondre_utilisateur ON ressources.repondre(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_repondre_question ON ressources.repondre(question_id);
CREATE INDEX IF NOT EXISTS idx_repondre_date ON ressources.repondre(date_reponse);

CREATE INDEX IF NOT EXISTS idx_exercise_programmer_prof ON ressources.exercises_programmer(programme_par_id);
CREATE INDEX IF NOT EXISTS idx_exercise_programmer_date_prevue ON ressources.exercises_programmer(date_exo_prevue);
CREATE INDEX IF NOT EXISTS idx_exercise_programmer_etat ON ressources.exercises_programmer(etat_exercise_programmer);
CREATE INDEX IF NOT EXISTS idx_exercise_programmer_classes_classe ON ressources.exercise_programmer_classes(classe_id);

CREATE INDEX IF NOT EXISTS idx_participer_exo_utilisateur ON ressources.participer_exo(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_participer_exo_exercise ON ressources.participer_exo(exercise_programmer_id);
CREATE INDEX IF NOT EXISTS idx_participer_exo_dates ON ressources.participer_exo(date_debut, date_fin);
