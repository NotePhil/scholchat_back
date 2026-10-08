package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatExercise;
import cmr.notep.modele.TypeAssignation;
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
    private String exerciseId;
    private String programmeParId;
    private TypeAssignation typeAssignation; // EXERCICE or DEVOIR
    private Date dateExoPrevue;
    private Date dateDebutExoEffectif;
    private Date dateFinExoEffectif;
    private EtatExercise etat;
    private List<String> classeIds;
    private List<String> coursIds; // optional: link to specific courses
    /**
     * Cours (programmé dans la/les classe(s) de diffusion) auquel rattacher cet exercice programmé.
     * Absent/vide = « Exercices généraux ». Sinon 400 COURS_NON_PROGRAMME_DANS_CLASSE.
     */
    private String coursId;
    /**
     * Cours choisi pour chaque classe : {classeId: coursId | null (« Exercice général »)}. Prioritaire sur coursId
     * pour les classes listées (coursId reste la valeur par défaut des autres classes). Les classes en clé sont
     * ajoutées à classeIds. Si toutes les classes ont le même cours, une seule programmation est créée ; sinon une
     * programmation par cours distinct (mêmes dates/type), chacune diffusée dans ses classes.
     */
    private java.util.Map<String, String> coursParClasse;
}