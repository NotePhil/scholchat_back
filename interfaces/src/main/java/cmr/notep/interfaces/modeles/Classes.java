package cmr.notep.interfaces.modeles;

import cmr.notep.modele.DroitPublication;
import cmr.notep.modele.EtatClasse;
import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
public class Classes implements Serializable {
    private String id;
    private String nom;
    private String niveau;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date dateCreation;

    private String codeActivation;
    private EtatClasse etat;
    private Etablissement etablissement;


    private Professeurs moderator;


    private DroitPublication droitPublication;
}