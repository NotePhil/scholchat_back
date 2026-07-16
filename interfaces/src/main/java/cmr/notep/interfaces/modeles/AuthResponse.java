package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String userId;
    private String userEmail;
    private String username;
    private String userType;
    private EtatUtilisateur userStatus;
    // Multi-role support
    private java.util.List<String> availableRoles;
    private String selectedRole;
    private boolean multiRole;
    // Parent-children info
    private java.util.List<ChildInfo> children;
    // Classes/etablissements dont l'offre est expiree (professeur/gestionnaire concerne) ;
    // vide/absent si aucune offre expiree. Voir ContratBusiness.resoudreEntitesExpirees.
    private java.util.List<java.util.Map<String, String>> expiredEntities;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChildInfo {
        private String id;
        private String nom;
        private String prenom;
        private String niveau;
    }
}