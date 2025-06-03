package cmr.notep.interfaces.modeles;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Professeurs extends Utilisateurs {
    private String cniUrlRecto;
    private String cniUrlVerso;
    //TODO : supprimer cette propriété pour etre conforme au diagramme de classe
//    private String nomEtablissement;
////    //TODO : mettre la relation avec la classe Canal qui sera en relation avec Classe
//    private String nomClasse;

    private String matriculeProfesseur;
//    private List<Matiere> matieresEnseignees;  un comment if we want to get the list of matiere tought by a teacher
}

