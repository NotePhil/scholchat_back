-- Migration pour ajouter les champs visibility et selectedClasses aux événements

-- Ajouter la colonne visibility
ALTER TABLE ressources.evenements ADD COLUMN IF NOT EXISTS visibility VARCHAR(20) DEFAULT 'PUBLIC';

-- Créer la table pour les classes sélectionnées des événements privés
CREATE TABLE IF NOT EXISTS ressources.evenement_classes (
    evenement_id VARCHAR(255) NOT NULL,
    classe_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (evenement_id, classe_id),
    FOREIGN KEY (evenement_id) REFERENCES ressources.evenements(id) ON DELETE CASCADE
);

-- Mettre à jour les événements existants avec une visibilité par défaut
UPDATE ressources.evenements SET visibility = 'PUBLIC' WHERE visibility IS NULL;