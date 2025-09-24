package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatCours;
import cmr.notep.modele.ListeNiveau;
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
public class ExerciseResponseDTO {
    private String id;
    private String nom;
    private String description;
    private Date dateCreation;
    private EtatCours etat;
    private String restriction;
    private ListeNiveau niveau;
    private String redacteurId;
    private List<CoursSummaryDTO> coursLies;
}