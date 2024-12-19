package cmr.notep.interfaces.modeles;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Eleves extends Utilisateurs {
    private String niveau;
    private List<Classes> classes;
}
