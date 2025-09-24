package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatCours;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursRequestDTO {
    private String titre;
    private String description;
    private EtatCours etat;
    private String references;
    private String restriction;
    private String redacteurId;
}
