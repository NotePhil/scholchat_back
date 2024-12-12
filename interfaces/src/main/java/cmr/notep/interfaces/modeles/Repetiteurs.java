package cmr.notep.interfaces.modeles;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class Repetiteurs extends Utilisateurs {

    private String cniUrlFront;
    private String cniUrlBack;
    private String pieceIdentite;
    private String photo;
}
