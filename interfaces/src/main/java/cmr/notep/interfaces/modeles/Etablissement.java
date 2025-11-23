package cmr.notep.interfaces.modeles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
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
    private boolean optionEnvoiMailVersClasse;
    private boolean optionTokenGeneral;
    private boolean codeUnique;
    private Utilisateurs gestionnaire;
}