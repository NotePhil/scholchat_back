package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatClasse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoActivation {
    private String id;
    private String classeId;
    private String utilisateurId;
    private LocalDateTime dateActivation;
    private LocalDateTime dateDesactivation;
    private String motifDesactivation;
    private boolean isActive;
    private EtatClasse etatClasse;
}