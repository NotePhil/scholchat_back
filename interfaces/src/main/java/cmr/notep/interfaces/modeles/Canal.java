package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"professeur", "classe"})
@EqualsAndHashCode(exclude = {"professeur", "classe"})
public class Canal implements Serializable {
    private String id;
    private String nom;
    private String description;
    private Professeurs professeur;
    private Classes classe;
}
