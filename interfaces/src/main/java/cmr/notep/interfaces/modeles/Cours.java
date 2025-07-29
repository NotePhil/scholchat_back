package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatCours;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Cours {
    private String id;
    private String titre;
    private String description;
    private Date dateCreation;
    private EtatCours etat;
    private String references;
    private String contenu;
    @JsonProperty("redacteurId") // Change to simple ID reference
    private String redacteurId;

    @JsonProperty("matiereIds") // Change to list of IDs
    private List<String> matiereIds;

    // These will be populated in the business layer
    @JsonIgnore
    private Professeurs redacteur;

    @JsonIgnore
    private List<Matiere> matieres;
}