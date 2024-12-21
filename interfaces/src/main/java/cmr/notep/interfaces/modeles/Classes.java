package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatClasse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"eleves", "parents"})
@EqualsAndHashCode(exclude = {"eleves", "parents"})
@JsonIgnoreProperties({"eleves", "parents"})
public class Classes implements Serializable {
    private String id;
    private String nom;
    private String niveau;
    private Date dateCreation;
    private String codeActivation;
    private EtatClasse etat;
    private Etablissement etablissement;
    private List<Parents> parents;
    private List<Eleves> eleves;
}
