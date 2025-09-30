package cmr.notep.interfaces.modeles;

import cmr.notep.modele.TypeQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionReponse {
    private String id;
    private String intitule;
    private String reponse;
    private TypeQuestion typeQuestion;
    private String exerciseId;
}