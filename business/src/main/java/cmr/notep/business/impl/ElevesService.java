package cmr.notep.business.impl;

import cmr.notep.business.business.ElevesBusiness;
import cmr.notep.interfaces.api.ElevesApi;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ElevesService implements ElevesApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;

    private final ElevesBusiness elevesBusiness;

    public ElevesService(ElevesBusiness elevesBusiness) {
        this.elevesBusiness = elevesBusiness;
    }

    @Override
    public Eleves avoirEleve(@NonNull String idEleve) {
        accessControl.requireRelatedUser(idEleve);
        return elevesBusiness.avoirEleve(idEleve);
    }

    @Override
    public List<Eleves> avoirToutEleves() {
        List<Eleves> tous = elevesBusiness.avoirToutEleves();
        if (currentUser.isAdmin()) return tous;
        // Non-admin : uniquement les élèves liés (mêmes classes, ses enfants) — plus d'annuaire global.
        java.util.Set<String> lies = accessControl.relatedUserIds(currentUser.requireUserId());
        return tous.stream().filter(e -> lies.contains(e.getId())).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Eleves posterEleve(@NonNull Eleves Eleve) {
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin()) {
            if (!currentUser.hasRole("PARENT")) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("Seuls un parent ou un administrateur peuvent créer un profil élève.");
            }
            // L'id fourni par le client ne doit jamais écraser un compte existant.
            if (Eleve.getId() == null || Eleve.getId().isBlank() || accessControl.userExists(Eleve.getId())) {
                Eleve.setId(java.util.UUID.randomUUID().toString());
            }
            Eleve.setAdmin(false);
            Eleve.setPasseAccess(null);
            Eleve.setActivationToken(null);
            Eleve.setResetPasswordToken(null);
            Eleve.setCreationDate(java.time.LocalDateTime.now());
        }
        return elevesBusiness.posterEleve(Eleve);
    }

    @Override
    public Eleves modifierElevePartiellement(@NonNull String idEleve, @NonNull Eleves partialEleve) {
        accessControl.requireSelfOrParentOrAdmin(idEleve);
        if (!currentUser.isAdmin()) {
            cmr.notep.interfaces.modeles.Utilisateurs existant = elevesBusiness.avoirEleve(idEleve);
            if (partialEleve.getEtat() != null && partialEleve.getEtat() != existant.getEtat()) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("Seul un administrateur peut modifier l'état d'un compte.");
            }
            if (partialEleve.getEmail() != null && !partialEleve.getEmail().trim().equalsIgnoreCase(String.valueOf(existant.getEmail()))) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("L'adresse email d'un compte ne peut être modifiée que par un administrateur.");
            }
            partialEleve.setEtat(null);
            partialEleve.setEmail(null);
        }
        return elevesBusiness.modifierElevePartiellement(idEleve, partialEleve);
    }
    @Override
    public List<Eleves> avoirElevesPourProfesseur(String professeurId) {
        currentUser.requireSelfOrAdmin(professeurId);
        return elevesBusiness.avoirElevesPourProfesseur(professeurId);
    }

    @Override
    public List<Parents> obtenirParents(String eleveId) {
        accessControl.requireRelatedUser(eleveId);
        return elevesBusiness.obtenirParents(eleveId);
    }
}