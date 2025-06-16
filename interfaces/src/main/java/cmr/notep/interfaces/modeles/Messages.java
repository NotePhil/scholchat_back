package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Messages implements Serializable {
    private String id;
    private String contenu;
    private String dateCreation;
    private String dateModification;
    private String etat;
    private String expediteur;
    private List<String> destinataires;

    private List<String> classeIds;

}
