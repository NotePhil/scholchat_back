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
public class QuestionReponseResponseDTO {
    private String id;
    private String intitule;
    private String reponse;
    private TypeQuestion typeQuestion;
    private String exerciseId;
    private Integer points;
    private List<ChoixReponseDTO> choixReponses;
}