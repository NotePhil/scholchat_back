package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatCoursProgramme;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoursProgrammer {
    private String id;
    private String coursId;
    private LocalDateTime dateCoursPrevue;
    private LocalDateTime dateDebutEffectif;
    private LocalDateTime dateFinEffectif;
    private EtatCoursProgramme etatCoursProgramme;
    private String classeId;
    private String lieu;
    private String description;
    private Integer capaciteMax;
    private List<String> participantsIds;
}