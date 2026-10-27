-- Messagerie (chat) : filtres et corbeille, par participant.
--   favori            : discussion mise en favori par l'utilisateur (filtre « Favoris ») ;
--   marque_non_lu     : « Marquer comme non lu » (filtre « Non lus »), effacé à la lecture ;
--   historique_depuis : suppression définitive depuis la corbeille : les messages antérieurs ou égaux
--                       à cette date ne sont plus jamais montrés à l'utilisateur (deleted_at = date de
--                       mise à la corbeille, restaurable tant qu'il n'est pas NULL).
-- Idempotent. Même contenu que StartupSchemaPatcher et schema-h2.sql.
ALTER TABLE ressources.conversation_participants ADD COLUMN IF NOT EXISTS favori BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ressources.conversation_participants ADD COLUMN IF NOT EXISTS marque_non_lu BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ressources.conversation_participants ADD COLUMN IF NOT EXISTS historique_depuis TIMESTAMP;
