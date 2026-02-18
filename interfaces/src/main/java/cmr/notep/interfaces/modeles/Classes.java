package cmr.notep.interfaces.modeles;

import cmr.notep.modele.DroitPublication;
import cmr.notep.modele.EtatClasse;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class Classes implements Serializable {
    private String id;
    private String nom;
    private String niveau;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date dateCreation;

    private String codeActivation;
    private EtatClasse etat;
    private Etablissement etablissement;
    private String etablissementToken; // For token validation during creation
    private boolean paymentRequired; // True when no establishment is provided
    private Professeurs moderator;
    private DroitPublication droitPublication;
    
    @JsonSetter("moderator")
    public void setModeratorFromJson(JsonNode node) {
        if (node == null || node.isNull()) {
            this.moderator = null;
        } else if (node.isTextual()) {
            // If it's a string, create a Professeurs object with just the ID
            Professeurs prof = new Professeurs();
            prof.setId(node.asText());
            this.moderator = prof;
        } else if (node.isObject()) {
            // If it's an object, handle it normally
            Professeurs prof = new Professeurs();
            if (node.has("id")) {
                prof.setId(node.get("id").asText());
            }
            this.moderator = prof;
        }
    }
}