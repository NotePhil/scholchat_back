package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatExercise;
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
public class ExerciseProgrammerRequestDTO {
    private String nom;
    private String description;
    private ListeNiveau niveau;
    private String restriction;
    private String redacteurId;
    private String programmeParId;
    private Date dateExoPrevue;
    private Date dateDebutExoEffectif;
    private Date dateFinExoEffectif;
    private EtatExercise etat;
    private List<String> matiereIds;
    private List<String> coursIds;
}