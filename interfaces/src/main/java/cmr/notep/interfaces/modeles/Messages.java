package cmr.notep.interfaces.modeles;

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
public class Messages implements Serializable {
    private String id;
    private String contenu;
    private String dateCreation;
    private String dateModification;
    private String etat;
    private Utilisateurs expediteur;
    private List<Utilisateurs> destinataires;
}