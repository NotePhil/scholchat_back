package cmr.notep.interfaces.modeles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeAccesDto {
    private String id;
    private String utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String utilisateurEmail;
    private String classeId;
    private String classeNom;
    private String codeActivation;
    private String etat;
    private Date dateDemande;
    private Date dateTraitement;
    private String motifRejet;
    private String typeUtilisateur;
}