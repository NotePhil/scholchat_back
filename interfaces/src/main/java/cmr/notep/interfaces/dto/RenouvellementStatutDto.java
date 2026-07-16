package cmr.notep.interfaces.dto;

import cmr.notep.interfaces.modeles.Contrat;
import cmr.notep.interfaces.modeles.Offre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenouvellementStatutDto {
    private String classeId;
    private String etablissementId;
    private String nom;
    private Contrat contratCourant;
    private List<Offre> offresDisponibles;
}
