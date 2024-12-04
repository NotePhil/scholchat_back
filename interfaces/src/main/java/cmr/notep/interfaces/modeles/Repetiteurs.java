package cmr.notep.interfaces.modeles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Repetiteurs extends Utilisateurs {
    private String cniUrlFront; // Front side of the CNI
    private String cniUrlBack;  // Back side of the CNI
    private String pieceIdentite; // Identification document
    private String photo; // Photo of the repetiteur
}
