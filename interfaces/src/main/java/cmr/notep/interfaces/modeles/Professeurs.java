package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Professeurs extends Utilisateurs {
    private String cniUrlRecto;
    private String cniUrlVerso;
    private String selfieUrl;
    private String matriculeProfesseur;
    private boolean hasUploaded;
    @JsonIdentityReference(alwaysAsId = true)
    private List<Classes> moderatedClasses;

    @JsonIgnore
    private boolean isModerator;
}