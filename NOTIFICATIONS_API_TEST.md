# Notifications API - Test avec Postman

## Configuration
- Base URL: `http://localhost:8486/scholchat`
- Authorization: Bearer Token (JWT)

## Endpoints

### 1. Get All User Notifications
```
GET /notifications
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
[
  {
    "id": "uuid",
    "userId": "user-id",
    "type": "ACCESS_REQUEST",
    "title": "Nouvelle demande d'accès",
    "message": "Jean Dupont a demandé l'accès à votre classe",
    "actorId": "student-id",
    "actorName": "Jean Dupont",
    "relatedEntityId": "class-id",
    "relatedEntityType": "CLASS",
    "createdAt": "2026-02-06T14:30:00",
    "read": false
  }
]
```

### 2. Get Unread Notifications Only
```
GET /notifications/unread
Authorization: Bearer <your_jwt_token>
```

### 3. Get Unread Count
```
GET /notifications/count
Authorization: Bearer <your_jwt_token>
```

**Response:**
```json
5
```

### 4. Mark Notification as Read
```
PATCH /notifications/{notificationId}/read
Authorization: Bearer <your_jwt_token>
```

### 5. Mark All as Read
```
PATCH /notifications/read-all
Authorization: Bearer <your_jwt_token>
```

### 6. Delete Notification
```
DELETE /notifications/{notificationId}
Authorization: Bearer <your_jwt_token>
```

### 7. Delete All Notifications
```
DELETE /notifications/all
Authorization: Bearer <your_jwt_token>
```

## Types de Notifications

- **ACCESS_REQUEST**: Demande d'accès à une classe
- **ACTIVITY_CREATED**: Nouvelle activité créée
- **CLASS_VALIDATED**: Classe validée par admin
- **ASSIGNMENT_GIVEN**: Nouveau devoir donné
- **MESSAGE_SENT**: Nouveau message
- **EVENT_UPDATED**: Événement modifié

## Test Scenario

### 1. Login as Professor
```
POST /auth/login
Content-Type: application/json

{
  "email": "professor@example.com",
  "password": "password"
}
```

### 2. Get Notifications
```
GET /notifications
Authorization: Bearer <token_from_step_1>
```

### 3. Mark First Notification as Read
```
PATCH /notifications/{id}/read
Authorization: Bearer <token_from_step_1>
```

### 4. Check Unread Count
```
GET /notifications/count
Authorization: Bearer <token_from_step_1>
```

### 5. Delete a Notification
```
DELETE /notifications/{id}
Authorization: Bearer <token_from_step_1>
```

## Notes

- Les notifications sont triées par date (plus récentes en premier)
- Chaque utilisateur ne voit que ses propres notifications
- Les admins voient toutes les notifications système
- La suppression d'une notification ne concerne que l'utilisateur qui la supprime
