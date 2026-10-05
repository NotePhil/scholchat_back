package cmr.notep.business.impl;

import cmr.notep.business.business.ParentsBusiness;
import cmr.notep.interfaces.api.ParentsApi;
import cmr.notep.interfaces.dto.ParentSummaryDto;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ParentsService implements ParentsApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;

    private final ParentsBusiness parentsBusiness;

    public ParentsService(ParentsBusiness ParentsBusiness) {
        this.parentsBusiness = ParentsBusiness;
    }

    @Override
    public Parents avoirParent(@NonNull String idProfilParent) {
        accessControl.requireRelatedUser(idProfilParent);
        return parentsBusiness.avoirParent(idProfilParent);
    }

    @Override
    public List<Parents> avoirToutParents() {
        currentUser.requireAdmin();
        return parentsBusiness.avoirToutParents();
    }

    @Override
    public List<ParentSummaryDto> avoirToutParentsSummary() {
        List<ParentSummaryDto> tous = parentsBusiness.avoirToutParentsSummary();
        if (currentUser.isAdmin()) return tous;
        java.util.Set<String> lies = accessControl.relatedUserIds(currentUser.requireUserId());
        return tous.stream().filter(p -> lies.contains(p.getId())).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Parents posterParent(@NonNull Parents profilParent) {
        currentUser.requireAdmin();
        return parentsBusiness.posterParent(profilParent);
    }

    @Override
    public Parents modifierParentPartiellement(@NonNull String idProfilParent, @NonNull Parents partialParent) {
        currentUser.requireSelfOrAdmin(idProfilParent);
        if (!currentUser.isAdmin()) {
            cmr.notep.interfaces.modeles.Utilisateurs existant = parentsBusiness.avoirParent(idProfilParent);
            if (partialParent.getEtat() != null && partialParent.getEtat() != existant.getEtat()) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("Seul un administrateur peut modifier l'état d'un compte.");
            }
            if (partialParent.getEmail() != null && !partialParent.getEmail().trim().equalsIgnoreCase(String.valueOf(existant.getEmail()))) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("L'adresse email d'un compte ne peut être modifiée que par un administrateur.");
            }
            partialParent.setEtat(null);
            partialParent.setEmail(null);
        }
        return parentsBusiness.modifierParentPartiellement(idProfilParent, partialParent);
    }

    @Override
    public void ajouterEnfant(String parentId, String eleveId) {
        accessControl.requireCanLinkChild(parentId, eleveId);
        parentsBusiness.ajouterEnfant(parentId, eleveId);
    }

    @Override
    public void retirerEnfant(String parentId, String eleveId) {
        currentUser.requireSelfOrAdmin(parentId);
        parentsBusiness.retirerEnfant(parentId, eleveId);
    }

    @Override
    public List<ParentSummaryDto> avoirParentsPourProfesseur(String professeurId) {
        currentUser.requireSelfOrAdmin(professeurId);
        return parentsBusiness.avoirParentsPourProfesseur(professeurId);
    }

    @Override
    public List<Eleves> obtenirEnfants(String parentId) {
        currentUser.requireSelfOrAdmin(parentId);
        return parentsBusiness.obtenirEnfants(parentId);
    }
}