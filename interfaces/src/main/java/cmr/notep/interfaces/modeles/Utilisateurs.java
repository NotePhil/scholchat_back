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
}