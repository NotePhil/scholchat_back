package cmr.notep.interfaces.modeles;
import lombok.*;
import lombok.experimental.SuperBuilder;
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Professeurs extends Utilisateurs {
    private String cniUrlRecto;
    private String cniUrlVerso;
    //TODO : supprimer cette propriété pour etre conforme au diagramme de classe
    private String nomEtablissement;
    private String matriculeProfesseur;
}

