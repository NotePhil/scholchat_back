package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import java.io.Serializable;

@Data
@EqualsAndHashCode(exclude = {"gestionnaire"})
@ToString(exclude = {"gestionnaire"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Etablissement implements Serializable {
    private String id;
    private String nom;
    private String localisation;
    private String pays;
    private String email;
    private String telephone;
    private boolean optionEnvoiMailNewClasse;
    private boolean optionTokenGeneral;
    private String codeUnique;
    @JsonBackReference
    private Utilisateurs gestionnaire;
    private String gestionnaireId; // Exposed for filtering
}