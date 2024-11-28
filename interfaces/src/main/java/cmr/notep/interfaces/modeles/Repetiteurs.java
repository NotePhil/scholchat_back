package cmr.notep.interfaces.modeles;
import lombok.*;

import jakarta.validation.constraints.NotNull;

/**
 * Représente le modèle de données pour un répétiteur.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Repetiteurs {

    private Long id; // ID auto-généré

    @NotNull
    private String pieceIdentite; // Pièce d'identité obligatoire

    @NotNull
    private String photo; // Photo obligatoire
}
