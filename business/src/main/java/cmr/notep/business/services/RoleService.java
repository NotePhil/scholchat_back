package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.*;
import cmr.notep.modele.EtatUtilisateur;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleService {

    public List<String> determineUserRoles(Utilisateurs utilisateur) {
        List<String> roles = new ArrayList<>();

        // Base role
        roles.add("ROLE_USER");

        // Admin role
        if (utilisateur.isAdmin()) {
            roles.add("ROLE_ADMIN");
        }

        // Type-specific roles
        if (utilisateur instanceof Professeurs) {
            roles.add("ROLE_PROFESSOR");
            if (utilisateur.getEtat() == EtatUtilisateur.VALIDATED) {
                roles.add("ROLE_VALIDATED_PROFESSOR");
            }
        } else if (utilisateur instanceof Eleves) {
            roles.add("ROLE_STUDENT");
        } else if (utilisateur instanceof Parents) {
            roles.add("ROLE_PARENT");
        } else if (utilisateur instanceof Repetiteurs) {
            roles.add("ROLE_TUTOR");
        }

        return roles;
    }
}