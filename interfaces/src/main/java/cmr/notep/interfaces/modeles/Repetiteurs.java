package cmr.notep.interfaces.modeles;

import lombok.*;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Repetiteurs extends Utilisateurs {
    //TODO : supprimer cette classe et n'utiliser que la classe Professeurs
    private String cniUrlFront;
    private String cniUrlBack;
    private String fullPicUrl;
    private String nomClasse;
}

