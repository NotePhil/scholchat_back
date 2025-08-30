package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatCoursProgramme;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CoursProgrammer {
    private String id;
    private String coursId;
    private String professeurId; // Ajout de la référence au professeur
    private LocalDateTime dateCoursPrevue;
    private LocalDateTime dateDebutEffectif;
    private LocalDateTime dateFinEffectif;
    private EtatCoursProgramme etatCoursProgramme;
    private String lieu;
    private String description;
    private List<String> classesIds; // Relation many-to-many avec les classes
    private List<String> participantsIds; // Participants invités
}