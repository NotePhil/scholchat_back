package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Membre d'une classe (GET /acceder/classes/{id}/utilisateurs).
 * Les champs de détail (état, coordonnées, date de création, niveau, matricule) ne sont
 * renseignés que pour les gestionnaires de la classe (ou un administrateur) ; ils sont
 * omis du JSON sinon (y compris dans les autres usages du DTO, ex. messages).
 */
@Data
public class UtilisateurSimpleDto {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String typeUtilisateur;

    // Détails réservés aux gestionnaires de la classe
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String etat;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String telephone;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String adresse;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime creationDate;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String niveau;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String matriculeProfesseur;
}
