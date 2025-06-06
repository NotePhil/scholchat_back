package cmr.notep.interfaces.modeles;

import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Professeurs extends Utilisateurs {
    private String cniUrlRecto;
    private String cniUrlVerso;
    private String selfieUrl;
    private String matriculeProfesseur;

    // Moderator fields
    private boolean isModerator;
    private List<String> moderatedClasses; // IDs of classes this professor moderates
}