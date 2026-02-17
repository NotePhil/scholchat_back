-- Migration pour ajouter la table notifications

CREATE TABLE IF NOT EXISTS ressources.notifications (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    actor_id VARCHAR(255),
    actor_name VARCHAR(255),
    related_entity_id VARCHAR(255),
    related_entity_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES ressources.utilisateurs(id) ON DELETE CASCADE
);

-- Index pour les requêtes par utilisateur
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON ressources.notifications(user_id);

-- Index pour le tri par date
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON ressources.notifications(created_at DESC);

-- Index pour filtrer par statut lu/non-lu
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON ressources.notifications(is_read);

-- Index partiel pour les notifications non-lues par utilisateur (optimise getUnreadCount et getUnreadNotifications)
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON ressources.notifications(user_id, is_read) WHERE is_read = FALSE;
