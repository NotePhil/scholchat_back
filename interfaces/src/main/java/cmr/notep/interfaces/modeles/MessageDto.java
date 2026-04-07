package cmr.notep.interfaces.modeles;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class MessageDto {
    private String id;
    private String objet;
    private String contenu;
    private String dateCreation;
    private String dateModification;
    private String etat;
    private UtilisateurSimpleDto expediteur;
    private List<UtilisateurSimpleDto> destinataires;
    private List<String> classeIds;
    // statut par destinataire
    private boolean lu;
    private boolean favori;
    private Date dateLecture;
}