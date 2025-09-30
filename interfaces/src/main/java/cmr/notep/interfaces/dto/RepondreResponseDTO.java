package cmr.notep.interfaces.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepondreResponseDTO {
    private String utilisateurId;
    private String questionId;
    private String note;
    private String appreciation;
    private String reponseUtilisateur;
    private LocalDateTime dateReponse;
    private Boolean estCorrecte;

    // Informations supplémentaires
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String questionIntitule;
    private String exerciseNom;
}