package cmr.notep.interfaces.dto;

import lombok.Data;
import java.util.List;

@Data
public class ClasseInfoDto {
    private String classeId;
    private String nomClasse;
    private String niveau;
    private boolean accesMajeur;
    private String moderateurNom;
    private String moderateurPrenom;
    private List<EleveInfoDto> elevesAssocies;
}

