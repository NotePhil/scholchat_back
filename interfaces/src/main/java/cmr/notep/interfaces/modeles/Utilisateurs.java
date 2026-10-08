package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatUtilisateur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"messagesEnvoyer", "messagesRecus"})
@EqualsAndHashCode(exclude = {"messagesEnvoyer", "messagesRecus", "etablissementsGeres"})
@JsonIgnoreProperties(value={"messagesEnvoyer", "messagesRecus"}, ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Utilisateurs.class, name = "utilisateur"),
        @JsonSubTypes.Type(value = Professeurs.class, name = "professeur"),
        @JsonSubTypes.Type(value = Eleves.class, name = "eleve"),
        @JsonSubTypes.Type(value = Repetiteurs.class, name = "repetiteur"),
        @JsonSubTypes.Type(value = Parents.class, name = "parent"),
        @JsonSubTypes.Type(value = Gestionnaires.class, name = "gestionnaire")
})
public class Utilisateurs implements Serializable, IUtilisateurs {
    private String id;
    @NonNull
    private String nom;
    @NonNull
    private String prenom;
    private String email;
    // Jetons secrets (le jeton d'activation est un JWT d'accès) : jamais renvoyés dans les réponses JSON.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String resetPasswordToken;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passeAccess;
    private String telephone;
    private String adresse;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String activationToken;
    private EtatUtilisateur etat;
    private LocalDateTime creationDate;
    private boolean admin;
    /**
     * Vrai tant que l'utilisateur se connecte avec le mot de passe temporaire reçu par e-mail (inscription
     * par code de classe approuvée) : il doit en choisir un nouveau (POST /auth/change-password).
     * Lecture seule côté API (jamais modifiable par un payload client).
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean mustChangePassword;
    /**
     * Entrée de POST /utilisateurs uniquement (jamais persisté, jamais renvoyé) : code d'activation de la
     * classe (codeActivation) — obligatoire pour l'inscription publique d'un nouveau compte parent / élève.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String codeClasse;
    private List<Messages> messagesEnvoyer;
    private List<Messages> messagesRecus;
    @JsonManagedReference
    private List<Etablissement> etablissementsGeres;
    /**
     * Réponse de POST /utilisateurs uniquement (jamais persisté, ignoré en entrée) :
     * CREATED (nouveau compte), ROLE_ADDED (rôle ajouté à un compte existant, utilisable tout de suite),
     * ROLE_PENDING_VALIDATION (rôle professeur demandé sur un compte existant : pièces + validation admin),
     * ACTIVATION_REQUIRED (rôle ajouté, le compte doit encore être activé via l'email reçu).
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String inscriptionStatut;
    /**
     * Réponse de POST /utilisateurs uniquement, pour une inscription PROFESSEUR (jamais persisté, ignoré
     * en entrée) : jeton signé de courte durée qui autorise, sans être connecté, le dépôt des pièces
     * justificatives de CE compte (en-tête X-Upload-Token sur PATCH /utilisateurs/{id},
     * POST /media/presigned-url et POST /media/proxy-upload).
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private String uploadToken;
    /**
     * Réponse de POST /utilisateurs uniquement (inscription parent / élève avec code de classe) :
     * EN_ATTENTE_APPROBATION_CLASSE — compte créé (ou demande ajoutée à un compte en attente), inactif,
     * sans mot de passe, en attente d'approbation de la demande d'accès par le responsable de la classe.
     * Pour un compte existant actif (ajout de profil + code), reprend la valeur d'inscriptionStatut.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private String statutInscription;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private String classeNom;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private String classeId;
    /** Réponse de POST /utilisateurs : vrai si une demande d'accès à la classe du code a été créée. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private Boolean demandeAccesCreee;
    /**
     * Réponse de GET /utilisateurs/{id} (soi-même ou admin) : profils du compte et leur état (voir RoleProfil).
     * Jamais persisté, ignoré en entrée.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private List<RoleProfil> profils;
    /**
     * Réponse de GET /utilisateurs/{id} (soi-même ou admin) pour un compte parent : vrai si au moins un de ses
     * enfants a été accepté dans une classe (sinon, en session PARENT, les routes hors liste blanche répondent
     * 403 PARENT_SANS_ENFANT_VALIDE). Absent pour un compte sans profil parent.
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private Boolean parentAEnfantValide;
}