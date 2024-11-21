package cmr.notep.interfaces.modeles;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Professeur {
    private Long id; // Auto-incremented ID
    private String urlCni; // Field for unique identification (not nullable)
    private String urlPhoto;
    private String nomEtablissement;
    private String nomClasse;
    private String matriculeProfesseur;
}
