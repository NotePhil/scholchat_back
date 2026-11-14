package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    /** Enfants du parent inscrits dans les classes du professeur (un élément par enfant et par classe). */
    @Builder.Default
    private List<Enfant> enfants = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Enfant {
        private String id;
        private String nom;
        private String prenom;
        private String classeId;
        private String classeNom;
        /** Élève majeur suivi par le parent : id du suivi (DELETE /demandes-suivi/{id} le retire) ; null sinon. */
        private String suiviId;
    }
}
