package cmr.notep.interfaces.modeles;



import cmr.notep.modele.EtatExercise;
import cmr.notep.modele.EtatX;
import cmr.notep.modele.ListeNiveau;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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

public class Exercise {
    private String id;
    private String nom;
    private String description;
    private Date dateCreation;
    private EtatExercise etat;
    private String restriction; // PUBLIC/PRIVE
    private ListeNiveau niveau;
    private String redacteurId;

    private List<Cours> coursLies;
    private List<Matiere> matieres;
    private List<QuestionReponse> questions;
}