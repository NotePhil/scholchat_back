package cmr.notep.interfaces.modeles;

import cmr.notep.ressourcesjpa.dao.EtatClasse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Classes implements Serializable {
    private String id;
    private String nom;
    private String niveau;
    private Date dateCreation;
    private EtatClasse etat;
    private Etablissement etablissement;
    private List<Parents> parents;
    private List<Eleves> eleves;
}
