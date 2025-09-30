package cmr.notep.interfaces.modeles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Repondre {
    private String utilisateurId;
    private String questionId;
    private String note;
    private String appreciation;
    private String reponseUtilisateur;
    private LocalDateTime dateReponse;
    private Boolean estCorrecte;

    // Relations (optionnel pour les détails)
    private Utilisateurs utilisateur;
    private QuestionReponse question;
}