SET search_path TO ressources;

-- Statut de vérification du PROFIL professeur, distinct de l'état du compte (utilisateurs.etat) :
--   DOCUMENTS_MANQUANTS   : pièces (CNI recto/verso + selfie) incomplètes
--   EN_ATTENTE_VALIDATION : pièces complètes, en attente de la décision de l'administrateur
--   VALIDE                : pièces vérifiées par l'administrateur -> droits professeur
--   REJETE                : pièces refusées (motif_rejet_verification), nouveau dépôt possible
-- Un compte peut être ACTIVE (connexion possible, activation "partielle") sans être VALIDE : seuls
-- les professeurs VALIDE obtiennent les droits professeur (voir ProfesseurVerificationService).
-- Colonnes lues/écrites uniquement en SQL natif (non mappées en JPA, pour qu'aucune sauvegarde
-- d'entité ne les efface).
--
-- Idempotent : l'ajout de la colonne ET le rattrapage ne s'exécutent que si la colonne n'existe pas
-- encore (le rattrapage ne doit jamais écraser des décisions prises ensuite par l'administrateur).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = 'ressources' AND table_name = 'professeurs'
                     AND column_name = 'statut_verification') THEN

        ALTER TABLE ressources.professeurs
            ADD COLUMN statut_verification VARCHAR(30) NOT NULL DEFAULT 'DOCUMENTS_MANQUANTS';

        -- Rattrapage. "Pièces complètes" = les 3 pièces renseignées.
        UPDATE ressources.professeurs p SET statut_verification = CASE
            WHEN u.etat = 'REJECTED' THEN 'REJETE'
            WHEN NULLIF(TRIM(p.cni_url_front), '') IS NULL
                 OR NULLIF(TRIM(p.cni_url_back), '') IS NULL
                 OR NULLIF(TRIM(p.selfie_url), '') IS NULL
                THEN 'DOCUMENTS_MANQUANTS'
            -- Pièces complètes, compte en attente de validation : l'administrateur doit encore décider.
            WHEN u.etat = 'AWAITING_VALIDATION' THEN 'EN_ATTENTE_VALIDATION'
            -- Compte actif (parent, élève…) dont la demande de rôle professeur n'est pas encore validée.
            WHEN EXISTS (SELECT 1 FROM ressources.user_roles r
                         WHERE r.utilisateur_id = u.id AND r.role_type = 'PROFESSOR' AND r.is_active = false)
                THEN 'EN_ATTENTE_VALIDATION'
            -- ACTIVE (ou validé en attente de mot de passe, suspendu…) avec pièces complètes : déjà validé.
            ELSE 'VALIDE'
        END
        FROM ressources.utilisateurs u
        WHERE u.id = p.professeurs_id;

        -- has_uploaded recalculé à partir des pièces réellement présentes.
        UPDATE ressources.professeurs SET has_uploaded =
            (NULLIF(TRIM(cni_url_front), '') IS NOT NULL AND NULLIF(TRIM(cni_url_back), '') IS NOT NULL
             AND NULLIF(TRIM(selfie_url), '') IS NOT NULL);
    END IF;

    ALTER TABLE ressources.professeurs ADD COLUMN IF NOT EXISTS motif_rejet_verification TEXT;
    ALTER TABLE ressources.professeurs ADD COLUMN IF NOT EXISTS date_statut_verification TIMESTAMPTZ;
    CREATE INDEX IF NOT EXISTS idx_professeurs_statut_verification ON ressources.professeurs(statut_verification);
END
$$;
