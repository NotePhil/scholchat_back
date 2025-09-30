package cmr.notep.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepondreRequestDTO {
    private String utilisateurId;
    private String questionId;
    private String note;
    private String appreciation;
    private String reponseUtilisateur;
    private Boolean estCorrecte;
}