SET search_path TO ressources;

CREATE TABLE IF NOT EXISTS offres (
    id VARCHAR(255) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    cible VARCHAR(50) NOT NULL,
    prix_mensuel NUMERIC(12,2),
    duree_mensuelle_minutes BIGINT,
    prix_annuel NUMERIC(12,2),
    duree_annuelle_minutes BIGINT,
    nombre_classes_inclues INTEGER,
    classes_bonus INTEGER,
    est_test BOOLEAN NOT NULL DEFAULT FALSE,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    date_creation TIMESTAMP,
    date_maj TIMESTAMP,
    created_by VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS contrats (
    id VARCHAR(255) PRIMARY KEY,
    offre_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255),
    etablissement_id VARCHAR(255),
    periodicite VARCHAR(20) NOT NULL,
    prix_paye NUMERIC(12,2),
    date_debut TIMESTAMP,
    date_fin TIMESTAMP,
    statut VARCHAR(30) NOT NULL,
    date_paiement TIMESTAMP,
    contrat_precedent_id VARCHAR(255),
    date_derniere_notification_expiration TIMESTAMP,
    renewal_token VARCHAR(1000),
    created_by VARCHAR(255),
    date_creation TIMESTAMP,
    FOREIGN KEY (offre_id) REFERENCES offres(id),
    FOREIGN KEY (classe_id) REFERENCES classes(id),
    FOREIGN KEY (etablissement_id) REFERENCES etablissements(id),
    FOREIGN KEY (contrat_precedent_id) REFERENCES contrats(id)
);

CREATE INDEX IF NOT EXISTS idx_contrats_classe_id ON contrats(classe_id);
CREATE INDEX IF NOT EXISTS idx_contrats_etablissement_id ON contrats(etablissement_id);
CREATE INDEX IF NOT EXISTS idx_contrats_statut_date_fin ON contrats(statut, date_fin);
CREATE INDEX IF NOT EXISTS idx_contrats_renewal_token ON contrats(renewal_token);

-- Offres de demarrage, editables/supprimables ensuite depuis l'ecran d'administration des offres.
INSERT INTO offres (id, nom, description, cible, prix_mensuel, duree_mensuelle_minutes, prix_annuel, duree_annuelle_minutes, nombre_classes_inclues, classes_bonus, est_test, actif, date_creation, date_maj, created_by)
VALUES
    ('00000000-offre-classe-standard', 'Classe Standard', 'Offre par defaut pour une classe independante (sans etablissement)', 'CLASSE', 3000.00, 43200, 15000.00, 525600, NULL, NULL, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system'),
    ('00000000-offre-etablissement-bronze', 'Etablissement Bronze', 'Forfait etablissement de demarrage : 5 classes incluses', 'ETABLISSEMENT', 5000.00, 43200, 50000.00, 525600, 5, 0, FALSE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system'),
    ('00000000-offre-test-expiration', 'Test Expiration Rapide', 'Offre de test pour verifier le pipeline expiration/desactivation (3 minutes)', 'CLASSE', 100.00, 3, NULL, NULL, NULL, NULL, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'system')
ON CONFLICT (id) DO NOTHING;
