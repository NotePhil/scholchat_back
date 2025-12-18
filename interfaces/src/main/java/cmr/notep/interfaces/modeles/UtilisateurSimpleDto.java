package cmr.notep.interfaces.modeles;

import lombok.Data;

@Data
public class UtilisateurSimpleDto {
    private String id;
    private String nom;
    private String prenom;
    private String email;
}