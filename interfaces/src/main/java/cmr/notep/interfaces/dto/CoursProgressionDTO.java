package cmr.notep.interfaces.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CoursProgressionDTO {
    private String coursId;
    private String utilisateurId;
    private int totalChapitres;
    private int chapitresCompletes;
    private int pourcentage;
    private List<String> chapitresCompletesIds;
}
