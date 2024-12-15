package cmr.notep.interfaces.modeles;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProfilEleves extends Utilisateurs {
    // Ajoutez ici des champs spécifiques à ProfilEleves si nécessaire
}
