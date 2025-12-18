package cmr.notep.interfaces.dto;

import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.modele.EtatClasse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseCreationResponseDto {
    private Classes classe;
    private String token;
    private EtatClasse etat;
    private boolean paymentRequired;
    private String message;
}