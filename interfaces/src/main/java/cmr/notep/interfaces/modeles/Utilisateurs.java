package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatUtilisateur;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonIgnoreProperties(value={"messagesEnvoyer", "messagesRecus"},ignoreUnknown = true)
public class Utilisateurs implements Serializable,IUtilisateurs {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    @JsonIgnore
    private String passeAccess;
    private String telephone;
    private String adresse;
    private String activationToken;
    private EtatUtilisateur etat;
    private LocalDateTime creationDate;
    private List<Messages> messagesEnvoyer;
    private List<Messages> messagesRecus;
}
