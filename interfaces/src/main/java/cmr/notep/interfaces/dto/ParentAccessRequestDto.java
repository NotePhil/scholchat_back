package cmr.notep.interfaces.dto;

import lombok.Data;
import java.util.List;

@Data
public class ParentAccessRequestDto {
    private String token;
    private String parentId;
    private String classeId;
    private List<String> elevesIds; // Pour accès majeur (élèves existants)
    private List<String> elevesNoms; // Pour accès mineur (nouveaux élèves)
    private List<String> elevesEmails; // Optionnel pour accès majeur
}