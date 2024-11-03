package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.List;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"messagesEnvoyer","messagesRecus"})
@EqualsAndHashCode(exclude = {"messagesEnvoyer","messagesRecus"})
@JsonIgnoreProperties({"messagesEnvoyer","messagesRecus"})
public class Utilisateurs implements Serializable {
    private  String id;
    private  String nom;
    private  String prenom;
    private  String email;
    @JsonIgnore
    private  String passeAccess;
    private  String telephone;
    private  String adresse;
    private  String etat;
    private  List<Messages> messagesEnvoyer;
    private  List<Messages> messagesRecus;
    
    
}