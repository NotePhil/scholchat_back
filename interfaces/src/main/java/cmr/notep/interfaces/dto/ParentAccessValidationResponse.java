package cmr.notep.interfaces.dto;

import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.interfaces.modeles.Eleves;
import lombok.Data;

import java.util.List;

@Data
public class ParentAccessValidationResponse {
    private boolean valid;
    private Classes classe;
    private Utilisateurs moderateur;
    private List<Eleves> eleves; // Pour autocomplétion si accesMajeur=true
    private boolean accesMajeur; // Pour déterminer le type de formulaire à afficher
}