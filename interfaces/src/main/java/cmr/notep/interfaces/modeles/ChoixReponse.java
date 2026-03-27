package cmr.notep.interfaces.modeles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChoixReponse {
    private String id;
    private String texte;
    private Boolean estCorrect;
    private Integer ordreAffichage;
}
