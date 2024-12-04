package cmr.notep.interfaces.modeles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Professeurs extends Utilisateurs {
    private String cniUrlFront;
    private String cniUrlBack;
    private String nomEtablissement;
    private String nomClasse;
    private String matriculeProfesseur;
}
