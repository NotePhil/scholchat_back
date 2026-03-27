SET search_path TO ressources;

-- Table to track multiple roles per user
-- A user can be professor AND parent at the same time
CREATE TABLE IF NOT EXISTS user_roles (
    id VARCHAR(255) PRIMARY KEY,
    utilisateur_id VARCHAR(255) NOT NULL,
    role_type VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    date_attribution TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_roles_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id),
    CONSTRAINT uk_user_role UNIQUE (utilisateur_id, role_type)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_utilisateur ON user_roles(utilisateur_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_type ON user_roles(role_type);

-- Populate user_roles from existing sub-tables
-- Professors
INSERT INTO user_roles (id, utilisateur_id, role_type, is_active)
SELECT gen_random_uuid()::text, professeurs_id, 'PROFESSOR', true
FROM professeurs
ON CONFLICT (utilisateur_id, role_type) DO NOTHING;

-- Parents
INSERT INTO user_roles (id, utilisateur_id, role_type, is_active)
SELECT gen_random_uuid()::text, parents_id, 'PARENT', true
FROM parents
ON CONFLICT (utilisateur_id, role_type) DO NOTHING;

-- Students
INSERT INTO user_roles (id, utilisateur_id, role_type, is_active)
SELECT gen_random_uuid()::text, eleves_id, 'STUDENT', true
FROM eleves
ON CONFLICT (utilisateur_id, role_type) DO NOTHING;

-- Gestionnaires
INSERT INTO user_roles (id, utilisateur_id, role_type, is_active)
SELECT gen_random_uuid()::text, gestionnaires_id, 'GESTIONNAIRE', true
FROM gestionnaires
ON CONFLICT (utilisateur_id, role_type) DO NOTHING;

-- Repetiteurs
INSERT INTO user_roles (id, utilisateur_id, role_type, is_active)
SELECT gen_random_uuid()::text, repetiteurs_id, 'TUTOR', true
FROM repetiteurs
ON CONFLICT (utilisateur_id, role_type) DO NOTHING;

-- Admins
INSERT INTO user_roles (id, utilisateur_id, role_type, is_active)
SELECT gen_random_uuid()::text, id, 'ADMIN', true
FROM utilisateurs WHERE is_admin = true
ON CONFLICT (utilisateur_id, role_type) DO NOTHING;
