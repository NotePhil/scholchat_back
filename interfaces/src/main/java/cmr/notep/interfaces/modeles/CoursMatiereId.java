package cmr.notep.interfaces.modeles;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
public class CoursMatiereId implements Serializable {
    private String coursId;
    private String matiereId;
}