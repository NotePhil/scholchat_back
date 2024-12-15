package cmr.notep.interfaces.modeles;
import lombok.*;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Professeurs extends Utilisateurs {
    private String cniUrlFront;
    private String cniUrlBack;
    private String nomEtablissement;
    private String nomClasse;
    private String matriculeProfesseur;
}

