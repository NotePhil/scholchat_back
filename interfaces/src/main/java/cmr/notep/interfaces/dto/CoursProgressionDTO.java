package cmr.notep.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursProgressionDTO {
    private String coursId;
    private String utilisateurId;
    private int totalChapitres;
    private int chapitresCompletes;
    private int pourcentage;
    private List<String> chapitresCompletesIds;
    /** Temps de lecture total (secondes) sur les chapitres terminés du cours. */
    private long tempsTotalSecondes;
    /** Un élément par chapitre du cours (dans l'ordre). */
    private List<Chapitre> chapitres;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Chapitre {
        private String chapitreId;
        private String titre;
        private boolean complete;
        private Date dateCompletion;
        /** Temps de lecture cumulé (secondes), 0 si non mesuré. */
        private long dureeSecondes;
    }
}
