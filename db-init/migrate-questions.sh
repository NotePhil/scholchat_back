#!/bin/bash

# Script pour exécuter la migration des questions

echo "Exécution de la migration pour les questions..."

sudo docker exec -i postgres psql -U scholchat_user -d scholchat < /home/kamsu-perold/scholchat_back/db-init/src/main/resources/sql/migration-questions.sql

if [ $? -eq 0 ]; then
    echo "✓ Migration exécutée avec succès!"
else
    echo "✗ Erreur lors de la migration"
    exit 1
fi
