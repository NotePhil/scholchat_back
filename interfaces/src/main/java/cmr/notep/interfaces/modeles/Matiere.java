package cmr.notep.interfaces.modeles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Matiere {
    private String id;
    private Matiere nom;
    private List<Professeurs> professeurs;
}