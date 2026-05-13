package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentSummaryDto {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private EtatUtilisateur etat;
    private LocalDateTime creationDate;
    private boolean admin;
}
