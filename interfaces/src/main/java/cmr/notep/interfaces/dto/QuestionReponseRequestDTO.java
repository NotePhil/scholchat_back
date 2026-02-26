package cmr.notep.interfaces.dto;

import cmr.notep.modele.TypeQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionReponseRequestDTO {
    private String intitule;
    private TypeQuestion typeQuestion;
    private Double points;
    
    // Pour QCM, ASSOCIATION, CLASSEMENT
    private List<ChoixReponseDTO> choixReponses;
    
    // Pour VRAI_FAUX
    private Boolean reponseAttendueVraiFaux;
    
    // Pour REPONSE_COURTE
    private String reponseAttendueCourte;
    
    // Pour REPONSE_LONGUE, TROU, DEVELOPPEMENT
    private String reponseAttendueLongue;
}
