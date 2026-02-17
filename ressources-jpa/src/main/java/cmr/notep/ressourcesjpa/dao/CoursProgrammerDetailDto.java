package cmr.notep.ressourcesjpa.dao;

import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.modele.EtatCoursProgramme;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO enrichi qui contient les détails complets des classes et participants
 */
@Data
public class CoursProgrammerDetailDto {
    private String id;
    private String coursId;
    private String coursTitre; // Titre du cours
    private String professeurId;
    private String professeurNom;
    private String professeurPrenom;
    private LocalDateTime dateCoursPrevue;
    private LocalDateTime dateDebutEffectif;
    private LocalDateTime dateFinEffectif;
    private EtatCoursProgramme etatCoursProgramme;
    private String lieu;
    private String description;

    // DÉTAILS COMPLETS DES CLASSES
    private List<ClasseDetailDto> classes;

    // DÉTAILS COMPLETS DES PARTICIPANTS EXPLICITEMENT INVITÉS
    private List<ParticipantDetailDto> participantsInvites;

    // TOUS LES ÉTUDIANTS QUI ONT ACCÈS VIA LES CLASSES
    private List<ParticipantDetailDto> tousLesEtudiantsAccessibles;

    @Data
    public static class ClasseDetailDto {
        private String id;
        private String nom;
        private String niveau;
        private int nombreEtudiants; // Nombre d'étudiants ayant accès à cette classe
    }

    @Data
    public static class ParticipantDetailDto {
        private String id;
        private String nom;
        private String prenom;
        private String email;
        private String telephone;
        private String typeUtilisateur; // ELEVE, PARENT, REPETITEUR, etc.
        private List<String> classesIds; // Classes auxquelles il a accès
    }
}