CREATE SCHEMA IF NOT EXISTS ressources;

SET search_path TO ressources;

-- Tables de base
CREATE TABLE IF NOT EXISTS utilisateurs (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255),
    prenom VARCHAR(255),
    email VARCHAR(255),
    passeaccess VARCHAR(255),
    telephone VARCHAR(255),
    adresse VARCHAR(255),
    etat VARCHAR(50) DEFAULT 'INACTIVE',
    activation_token VARCHAR(255) UNIQUE,
    creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_admin BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS etablissements (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    localisation VARCHAR(255),
    pays VARCHAR(255),
    email VARCHAR(255),
    telephone VARCHAR(255),
    option_envoi_mail_classe BOOLEAN DEFAULT FALSE,
    option_token_general BOOLEAN DEFAULT FALSE,
    code_unique BOOLEAN DEFAULT FALSE,
    token_general VARCHAR(8),
    code_unique_value VARCHAR(6),
    gestionnaire_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS professeurs (
    professeurs_id VARCHAR(255) PRIMARY KEY,
    cni_url_front VARCHAR(255),
    cni_url_back VARCHAR(255),
    selfie_url VARCHAR(255),
    matricule_professeur VARCHAR(255) UNIQUE,
    has_uploaded BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS classes (
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

CREATE TABLE IF NOT EXISTS parents (
    parents_id VARCHAR(255) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS eleves (
    eleves_id VARCHAR(255) PRIMARY KEY,
    niveau VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS repetiteurs (
    repetiteurs_id VARCHAR(255) PRIMARY KEY,
    cni_url_front VARCHAR(255) NOT NULL,
    cni_url_back VARCHAR(255) NOT NULL,
    photo_full_picture VARCHAR(255) NOT NULL,
    nom_classe VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS matieres (
    id UUID PRIMARY KEY,
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
    id VARCHAR(255) PRIMARY KEY,
    contenu VARCHAR(255),
    datecreation VARCHAR(255),
    datemodification VARCHAR(255),
    etat VARCHAR(255),
    expediteur_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS evenements (
    id UUID PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    lieu VARCHAR(255),
    etat VARCHAR(50) NOT NULL,
    heure_debut TIMESTAMP NOT NULL,
    heure_fin TIMESTAMP,
    createur_id VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS professeur_classes_moderees (
    professeur_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL,
    PRIMARY KEY (professeur_id, classe_id)
);

CREATE TABLE IF NOT EXISTS classe_parents (
    classe_id UUID NOT NULL,
    parent_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (classe_id, parent_id)
);

CREATE TABLE IF NOT EXISTS classe_eleves (
    classe_id UUID NOT NULL,
    eleve_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (classe_id, eleve_id)
);

CREATE TABLE IF NOT EXISTS canaux (
    id UUID PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    professeur_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS professeur_matiere (
    professeur_id VARCHAR(255) NOT NULL,
    matiere_id UUID NOT NULL,
    PRIMARY KEY (professeur_id, matiere_id)
);

CREATE TABLE IF NOT EXISTS classe_matieres (
    matiere_id UUID NOT NULL,
    classe_id UUID NOT NULL,
    PRIMARY KEY (matiere_id, classe_id)
);

CREATE TABLE IF NOT EXISTS droit_publication (
    utilisateur_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL,
    date_attribution TIMESTAMP NOT NULL,
    peut_publier BOOLEAN NOT NULL DEFAULT FALSE,
    peut_moderer BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (utilisateur_id, classe_id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id),
    FOREIGN KEY (classe_id) REFERENCES classes(id)
);

CREATE TABLE IF NOT EXISTS recevoir (
    message_id VARCHAR(255) NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS evenement_participants (
    evenement_id UUID NOT NULL,
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
    evenement_id UUID
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
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    utilisateur_id UUID NOT NULL
);

CREATE TABLE IF NOT EXISTS histo_activation (
    id UUID PRIMARY KEY,
    classe_id UUID NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    date_activation TIMESTAMP NOT NULL,
    date_desactivation TIMESTAMP,
    motif_desactivation VARCHAR(255),
    is_active BOOLEAN NOT NULL,
    etat_classe VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS message_classes (
    message_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL,
    PRIMARY KEY (message_id, classe_id),
    FOREIGN KEY (message_id) REFERENCES messages(id),
    FOREIGN KEY (classe_id) REFERENCES classes(id)
);

CREATE TABLE IF NOT EXISTS acceder (
    utilisateur_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL,
    date_acces TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (utilisateur_id, classe_id),
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id),
    FOREIGN KEY (classe_id) REFERENCES classes(id)
);

CREATE TABLE IF NOT EXISTS demandes_acces (
    id VARCHAR(255) PRIMARY KEY,
    utilisateur_id VARCHAR(255) NOT NULL,
    classe_id UUID NOT NULL,
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
    id UUID PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_creation TIMESTAMP NOT NULL,
    etat VARCHAR(50) NOT NULL,
    reference TEXT,
    contenu TEXT NOT NULL,
    redacteur_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (redacteur_id) REFERENCES professeurs(professeurs_id)
);

CREATE TABLE IF NOT EXISTS cours_matiere (
    cours_id UUID NOT NULL,
    matiere_id UUID NOT NULL,
    date_ajout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ordre_dans_cours INTEGER,
    PRIMARY KEY (cours_id, matiere_id),
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE,
    FOREIGN KEY (matiere_id) REFERENCES matieres(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS chapitres (
    id UUID PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    ordre INTEGER NOT NULL,
    contenu TEXT NOT NULL,
    image_url VARCHAR(255),
    cours_id UUID NOT NULL,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cours_programmer (
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
    FOREIGN KEY (cours_id) REFERENCES cours(id),
    FOREIGN KEY (classe_id) REFERENCES classes(id)
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
    classe_id UUID NOT NULL,
    PRIMARY KEY (cours_programmer_id, classe_id),
    FOREIGN KEY (cours_programmer_id) REFERENCES cours_programmer(id) ON DELETE CASCADE,
    FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS exercises (
    id UUID PRIMARY KEY,
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
    exercise_id UUID NOT NULL,
    cours_id UUID NOT NULL,
    date_liaison TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exercise_id, cours_id),
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    FOREIGN KEY (cours_id) REFERENCES cours(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS questions_reponses (
    id UUID PRIMARY KEY,
    intitule VARCHAR(1000) NOT NULL,
    reponse TEXT,
    type_question VARCHAR(50) NOT NULL,
    exercise_id UUID NOT NULL,
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS exercise_matieres (
    exercise_id UUID NOT NULL,
    matiere_id UUID NOT NULL,
    date_association TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (exercise_id, matiere_id),
    FOREIGN KEY (exercise_id) REFERENCES exercises(id) ON DELETE CASCADE,
    FOREIGN KEY (matiere_id) REFERENCES matieres(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS repondre (
    utilisateur_id VARCHAR(255) NOT NULL,
    question_id UUID NOT NULL,
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
    exercise_id UUID PRIMARY KEY,
    date_exo_prevue TIMESTAMP NOT NULL,
    date_debut_exo_effectif TIMESTAMP NOT NULL,
    date_fin_exo_effectif TIMESTAMP NOT NULL,
    etat_exo_programme VARCHAR(50) NOT NULL,
    date_creation TIMESTAMP,
    date_modification TIMESTAMP,
    cree_par VARCHAR(255),
    modifie_par VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS exercises_programmer_participants (
    exercises_programmer_id UUID NOT NULL,
    utilisateur_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (exercises_programmer_id, utilisateur_id),
    FOREIGN KEY (exercises_programmer_id) REFERENCES exercises_programmer(exercise_id) ON DELETE CASCADE,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS exercises_programmer_classes (
    exercises_programmer_id UUID NOT NULL,
    classe_id UUID NOT NULL,
    PRIMARY KEY (exercises_programmer_id, classe_id),
    FOREIGN KEY (exercises_programmer_id) REFERENCES exercises_programmer(exercise_id) ON DELETE CASCADE,
    FOREIGN KEY (classe_id) REFERENCES classes(id) ON DELETE CASCADE
);
