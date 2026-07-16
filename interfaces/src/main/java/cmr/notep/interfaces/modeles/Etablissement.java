package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonBackReference;
import cmr.notep.interfaces.dto.PaymentInfoDto;
import cmr.notep.modele.PeriodiciteContrat;
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
    // Offre/Contrat (facultatif, uniquement a la creation) : voir ContratBusiness.
    private String offreId;
    private PeriodiciteContrat periodicite;
    private PaymentInfoDto paymentInfo;
    // true si l'offre de cet etablissement (et donc de toutes ses classes) a expire
    private boolean expireParOffre;
}