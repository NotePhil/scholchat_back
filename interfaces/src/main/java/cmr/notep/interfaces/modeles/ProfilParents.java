package cmr.notep.interfaces.modeles;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProfilParents extends Utilisateurs {
    private String cniUrlFront;
    private String cniUrlBack;
    private String fullPicUrl;
}

