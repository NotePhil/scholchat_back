# Module `db-init`

Module Spring Boot dédié à l'initialisation de la base PostgreSQL (création du schéma + données de référence) via Liquibase.

## Profils Spring

- `local` (par défaut) :
  - Cible une base PostgreSQL locale.
- `prod` :
  - Pensé pour l'environnement IaC (variables d'environnement / secrets).

## Lancement en local

### Prérequis

- PostgreSQL démarré (par exemple sur `localhost:5432`).
- Base créée (par exemple `scholchat`) et utilisateur avec les droits (par exemple `scholchat/scholchat`).

### Commandes

Depuis la racine du projet :

```bash
mvn -pl db-init clean package
mvn -pl db-init spring-boot:run -Dspring-boot.run.profiles=local
```

Cela applique les scripts Liquibase :

1. `sql/schema-postgres.sql` (création du schéma + tables)
2. `sql/data-postgres.sql` (insertion des données)

## Lancement en prod / IaC

Le module est pensé comme un **job one-shot** : on exécute le container une fois pour initialiser / migrer la base.

### Construction de l'image Docker

Toujours à la racine :

```bash
mvn -pl db-init clean package
# Image locale
cd db-init
docker build -t scholchat/db-init:latest .
```

### Exécution du job (exemples)

#### Docker simple

```bash
docker run --rm \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/scholchat \
  -e SPRING_DATASOURCE_USERNAME=scholchat \
  -e SPRING_DATASOURCE_PASSWORD=****** \
  scholchat/db-init:latest
```

#### Kubernetes (Job one-shot)

```yaml
apiVersion: batch/v1
kind: Job
metadata:
  name: db-init
spec:
  template:
    spec:
      restartPolicy: Never
      containers:
        - name: db-init
          image: scholchat/db-init:latest
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: SPRING_DATASOURCE_URL
              value: "jdbc:postgresql://postgres:5432/scholchat"
            - name: SPRING_DATASOURCE_USERNAME
              valueFrom:
                secretKeyRef:
                  name: db-secret
                  key: username
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: db-secret
                  key: password
```

Dans ce mode, l'unicité est gérée par l'orchestrateur (un seul Job lancé à la fois).

## Remarques

- Les scripts H2 (`business/src/main/resources/schema-h2.sql` et `data-h2.sql`) restent inchangés et sont utilisés pour les tests d'intégration.
- Le module `db-init` est uniquement responsable de PostgreSQL.

