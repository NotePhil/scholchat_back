package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapitre {
    private String id;
    private String titre;
    private String description;
    private Integer ordre;
    private String contenu;
//    private String imageMediaId;  // ID du média dans la table media
    private String imageUrl;

    private String coursId;

}