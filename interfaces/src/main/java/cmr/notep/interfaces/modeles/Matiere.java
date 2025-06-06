package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatMatieres;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Matiere {
    private String id;
    private String nom;
    private String description;
    private LocalDateTime dateCreation;
    private EtatMatieres etat;
    private List<Professeurs> professeurs;
    private List<Classes> classes;
}