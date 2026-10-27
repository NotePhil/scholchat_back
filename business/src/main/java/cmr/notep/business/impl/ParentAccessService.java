package cmr.notep.business.impl;

import cmr.notep.business.business.ParentAccessBusiness;
import cmr.notep.interfaces.api.ParentAccessApi;
import cmr.notep.interfaces.dto.ClasseInfoDto;
import cmr.notep.interfaces.dto.ParentAccessRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ParentAccessService implements ParentAccessApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;


    private final ParentAccessBusiness parentAccessBusiness;

    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.services.InscriptionParentService inscriptionParentService;

    @Override
    public ClasseInfoDto validerTokenEtRecupererInfos(String token, String classId) {
        currentUser.requireAuthenticated();
        log.info("API - Validation du token et récupération des infos de classe");
        return parentAccessBusiness.validerTokenEtRecupererInfos(token, classId);
    }

    @Override
    public void traiterDemandeAcces(ParentAccessRequestDto request) {
        currentUser.requireSelfOrAdmin(request.getParentId());
        if (!currentUser.isAdmin()) {
            // Élèves existants : uniquement des enfants rattachés à ce parent ET mineurs (un enfant majeur fait
            // ses demandes d'accès lui-même ; un élève non rattaché ne peut pas être "pris" par ce flux).
            if (request.getElevesIds() != null) {
                for (String eleveId : request.getElevesIds()) {
                    if (!accessControl.isParentOf(request.getParentId(), eleveId)) {
                        throw cmr.notep.business.security.CurrentUserService.forbidden(
                                "Cet élève n'est pas rattaché à votre compte.");
                    }
                    if (!accessControl.isMinorChildAccount(eleveId)) {
                        throw cmr.notep.business.security.CurrentUserService.forbidden(
                                cmr.notep.business.security.AccessControlService.MSG_ENFANT_MAJEUR_LECTURE_SEULE);
                    }
                }
            }
            // Nouveaux enfants par nom : pas de doublon d'un élève ayant son propre compte dans la classe
            if (request.getElevesNoms() != null && request.getClasseId() != null) {
                for (String n : request.getElevesNoms()) {
                    if (n == null) continue;
                    String[] parts = n.trim().split(" ", 2);
                    inscriptionParentService.verifierPasDElevePorteurDeCompte(request.getClasseId(),
                            parts.length > 1 ? parts[1] : "", parts[0]);
                }
            }
        }
        log.info("API - Traitement demande d'accès parent pour la classe {}", request.getClasseId());
        parentAccessBusiness.traiterDemandeAcces(request);
    }
}
