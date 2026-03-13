package cmr.notep.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChoixReponseDTO {
    private String id;
    private String texte;
    private Boolean estCorrect;
    private Integer ordreAffichage;
}
