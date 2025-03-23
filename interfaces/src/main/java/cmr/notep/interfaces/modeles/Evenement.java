package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatEvenement;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evenement {
    private String id;
    private String canalId;
    private String titre;
    private String description;
    private String lieu;
    private LocalDateTime heureDebut;
    private LocalDateTime heureFin;
    private EtatEvenement etat;
}