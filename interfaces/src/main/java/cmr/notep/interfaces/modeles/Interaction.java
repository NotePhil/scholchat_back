package cmr.notep.interfaces.modeles;

import cmr.notep.modele.TypeInteraction;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {
    private String id;
    private TypeInteraction type;
    private String contenu;
    private LocalDateTime dateCreation;
    private String niveau;
    private String evenementId;
}