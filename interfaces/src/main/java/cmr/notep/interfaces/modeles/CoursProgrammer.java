package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatCoursProgramme;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoursProgrammer {
    private String id;
    private String coursId;
    private String professeurId;
    private LocalDateTime dateCoursPrevue;
    private LocalDateTime dateDebutEffectif;
    private LocalDateTime dateFinEffectif;
    private EtatCoursProgramme etatCoursProgramme;
    private String lieu;
    private String description;

    // Liste des IDs des classes
    private List<String> classesIds;

    // Liste des IDs des participants
    private List<String> participantsIds;

    // NOUVEAUX CHAMPS : Informations détaillées
    private List<ClasseInfo> classes;
    private List<ParticipantInfo> participants;

    // Classes internes pour les informations détaillées
    @Data
    public static class ClasseInfo {
        private String id;
        private String nom;
        private String niveau;
    }

    @Data
    public static class ParticipantInfo {
        private String id;
        private String nom;
        private String prenom;
        private String email;
    }
}