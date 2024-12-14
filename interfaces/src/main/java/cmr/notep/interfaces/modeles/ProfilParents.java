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
@ToString(exclude = {"messagesEnvoyer", "messagesRecus"})
@EqualsAndHashCode(exclude = {"messagesEnvoyer", "messagesRecus"})
@JsonIgnoreProperties({"messagesEnvoyer", "messagesRecus"})
public class ProfilParents implements Serializable {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    @JsonIgnore
    private String passeAccess;
    private String telephone;
    private String adresse;
    private String etat;

    // New fields matching the SQL insert
    private String cniUrlFront;
    private String cniUrlBack;
    private String fullPicUrl;  // Corresponds to the 'full_pic_url' in SQL

    private List<Messages> messagesEnvoyer;
    private List<Messages> messagesRecus;
}