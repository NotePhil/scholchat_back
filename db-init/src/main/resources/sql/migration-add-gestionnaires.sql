-- Migration to add gestionnaires table and sample data
SET search_path TO ressources;

-- Create gestionnaires table
CREATE TABLE IF NOT EXISTS gestionnaires (
    gestionnaires_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (gestionnaires_id),
    CONSTRAINT fk_gestionnaires_utilisateurs
    FOREIGN KEY (gestionnaires_id) REFERENCES utilisateurs(id)
);

-- Insert sample gestionnaire users: Guy and Prince
INSERT INTO utilisateurs (id, nom, prenom, email, telephone, adresse, etat, is_admin, creation_date)
VALUES 
    ('gest-guy-001', 'Kamga', 'Guy', 'guy@scholchat.com', '+237670000001', 'Douala, Cameroun', 'ACTIVE', false, CURRENT_TIMESTAMP),
    ('gest-prince-001', 'Nkengfack', 'Prince', 'prince@scholchat.com', '+237670000002', 'Yaoundé, Cameroun', 'ACTIVE', false, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Insert into gestionnaires table
INSERT INTO gestionnaires (gestionnaires_id)
VALUES 
    ('gest-guy-001'),
    ('gest-prince-001')
ON CONFLICT (gestionnaires_id) DO NOTHING;
