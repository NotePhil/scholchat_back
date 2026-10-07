-- Inscription parent / élève majeur avec code de classe : le compte est créé sans mot de passe, puis activé
-- à l'approbation de la demande d'accès par le responsable de la classe avec un mot de passe temporaire
-- envoyé par e-mail. Tant que ce drapeau est vrai, l'utilisateur doit choisir un nouveau mot de passe
-- (POST /auth/change-password) avant de pouvoir utiliser l'application.
SET search_path TO ressources;

ALTER TABLE ressources.utilisateurs ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
