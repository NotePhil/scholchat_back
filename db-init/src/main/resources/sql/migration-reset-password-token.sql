-- Token of the e-mailed password reset link (POST /auth/reset-password-request, then
-- /auth/reset-password): stored on the user so a link can only be used once.
SET search_path TO ressources;

ALTER TABLE utilisateurs ADD COLUMN IF NOT EXISTS reset_password_token TEXT;
