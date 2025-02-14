package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatUtilisateur;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Utilisateurs.class, name = "utilisateur"),
        @JsonSubTypes.Type(value = Professeurs.class, name = "professeur"),
        @JsonSubTypes.Type(value = Eleves.class, name = "eleve"),
        @JsonSubTypes.Type(value = Repetiteurs.class, name = "repetiteur"),
        @JsonSubTypes.Type(value = Parents.class, name = "parent")
})
public interface IUtilisateurs {
    String getId();
    String getNom();
    String getPrenom();
    String getEmail();
    String getTelephone();
    String getAdresse();
    String getActivationToken();
    EtatUtilisateur getEtat();
    LocalDateTime getCreationDate();

    void setId(String id);
    void setNom(String nom);
    void setPrenom(String prenom);
    void setEmail(String email);
    void setTelephone(String telephone);
    void setAdresse(String adresse);
    void setActivationToken(String activationToken);
    void setEtat(EtatUtilisateur etat);
    void setCreationDate(LocalDateTime creationDate);
}