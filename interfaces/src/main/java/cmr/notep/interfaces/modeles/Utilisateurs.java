package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatUtilisateur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@EqualsAndHashCode(exclude = {"messagesEnvoyer", "messagesRecus"})
@JsonIgnoreProperties(value={"messagesEnvoyer", "messagesRecus"}, ignoreUnknown = true)
public class Utilisateurs implements Serializable, IUtilisateurs {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String passeAccess;
    private String telephone;
    private String adresse;
    private String activationToken;
    private EtatUtilisateur etat;
    private LocalDateTime creationDate;
    private boolean admin;
    private List<Messages> messagesEnvoyer;
    private List<Messages> messagesRecus;

    @Override
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public void setEtat(EtatUtilisateur etat) {
        this.etat = etat;
    }

    @Override
    public void setActivationToken(String activationToken) {
        this.activationToken = activationToken;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNom() {
        return nom;
    }

    @Override
    public String getPrenom() {
        return prenom;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getTelephone() {
        return telephone;
    }

    @Override
    public String getAdresse() {
        return adresse;
    }

    @Override
    public String getActivationToken() {
        return activationToken;
    }

    @Override
    public EtatUtilisateur getEtat() {
        return etat;
    }

    @Override
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    @Override
    public boolean isAdmin() {
        return admin;
    }

    @Override
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}