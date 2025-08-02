package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatCoursProgramme;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoursProgrammer extends Cours {
    private String id;
    private LocalDateTime dateCoursPrevue;
    private LocalDateTime dateDebutCoursEffectif;
    private LocalDateTime dateFinCoursEffectif;
    private EtatCoursProgramme etatCoursProgramme;
    private String classeId;
    private List<String> participantsIds;
}