# Guide de Création de Classe - Nouvelles Fonctionnalités

## Vue d'ensemble

Ce guide décrit les nouvelles fonctionnalités implémentées pour la création de classes avec gestion optionnelle d'établissement et paiement.

## Fonctionnalités Implémentées

### 1. Modification de l'Établissement

#### Nouveaux champs dans `EtablissementEntity` :
- `optionEnvoiMailNewClasse` (boolean) : Active l'envoi d'email lors de la création d'une nouvelle classe
- `optionTokenGeneral` (boolean) : Active la validation par code unique
- `codeUnique` (String, unique) : Code unique auto-généré pour l'établissement

#### Champs supprimés :
- `optionEnvoiMailVersClasse`
- `tokenGeneral`
- `codeUniqueValue`

### 2. Nouvelle API de Création de Classe

#### Endpoint : `POST /classes/nouvelle`

**Request Body :**
```json
{
  "nom": "Classe de Mathématiques",
  "niveau": "6ème",
  "etablissementId": "uuid-etablissement", // Optionnel
  "codeUnique": "ETB-12345678", // Requis si établissement avec optionTokenGeneral = true
  "moderatorId": "uuid-moderateur", // Optionnel
  "paymentInfo": { // Requis si pas d'établissement
    "cardNumber": "1234567890123456",
    "expiryDate": "12/25",
    "cvv": "123",
    "cardHolderName": "John Doe",
    "amount": 50.0
  }
}
```

**Response :**
```json
{
  "classe": {
    "id": "uuid-classe",
    "nom": "Classe de Mathématiques",
    "niveau": "6ème",
    "etat": "EN_ATTENTE_APPROBATION",
    "dateCreation": "2025-01-27T10:00:00Z"
  },
  "token": "generated-token-for-professor",
  "etat": "EN_ATTENTE_APPROBATION",
  "paymentRequired": false,
  "message": "Classe créée en attente de validation"
}
```

### 3. Logique de Création

#### Cas 1 : Avec Établissement
1. **Validation du code unique** (si `optionTokenGeneral = true`)
   - Le professeur doit fournir le `codeUnique` de l'établissement
   - Validation contre le champ `codeUnique` de l'établissement

2. **Sauvegarde de la classe**
   - Statut : `EN_ATTENTE_APPROBATION`
   - Génération d'un token pour le professeur

3. **Envoi d'email** (si `optionEnvoiMailNewClasse = true`)
   - Email de notification à l'établissement
   - Template : `class-creation-notification.html`

#### Cas 2 : Sans Établissement
1. **Traitement du paiement**
   - Validation des informations de paiement
   - Simulation du traitement (service fictif)

2. **Sauvegarde de la classe**
   - Statut : `ACTIF` (si paiement réussi)
   - `paymentRequired = true`
   - Génération d'un token pour le professeur

### 4. Services Créés

#### `TokenService`
- `generateClassToken()` : Génère un token sécurisé pour la classe
- `generateUniqueCode()` : Génère un code unique pour l'établissement (format: ETB-XXXXXXXX)

#### `PaymentService`
- `processPayment(PaymentInfoDto)` : Traite le paiement fictif
- Validation des champs requis (numéro de carte, CVV, date d'expiration)

### 5. Endpoints d'Approbation

#### `PATCH /classes/approve-by-establishment`
- Paramètres : `classeId`, `etablissementId`
- Approuve une classe en attente par l'établissement

#### `PATCH /classes/reject-by-establishment`
- Paramètres : `classeId`, `etablissementId`
- Rejette une classe en attente par l'établissement

### 6. Templates d'Email

#### `class-creation-notification.html`
Template pour notifier l'établissement de la création d'une nouvelle classe.

Variables disponibles :
- `classe` : Informations de la classe
- `etablissement` : Informations de l'établissement
- `dateCreation` : Date de création

## Utilisation

### Création avec Établissement
```bash
curl -X POST http://localhost:8486/scholchat/classes/nouvelle \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Classe Test",
    "niveau": "6ème",
    "etablissementId": "etab-123",
    "codeUnique": "ETB-12345678"
  }'
```

### Création avec Paiement
```bash
curl -X POST http://localhost:8486/scholchat/classes/nouvelle \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Classe Payante",
    "niveau": "5ème",
    "paymentInfo": {
      "cardNumber": "1234567890123456",
      "expiryDate": "12/25",
      "cvv": "123",
      "cardHolderName": "John Doe",
      "amount": 50.0
    }
  }'
```

## Configuration

### Base de données
Le schéma a été mis à jour dans `schema-h2.sql` pour refléter les nouveaux champs.

### Propriétés d'application
Les URLs d'approbation et de rejet sont configurées dans `application.properties` :
- `app.class-approval-url`
- `app.class-rejection-url`

## Codes d'erreur

- `PAYMENT_FAILED` : Échec du traitement du paiement
- `INVALID_INPUT` : Données d'entrée invalides
- `UNAUTHORIZED` : Code unique invalide
- `NOT_FOUND` : Établissement ou modérateur introuvable