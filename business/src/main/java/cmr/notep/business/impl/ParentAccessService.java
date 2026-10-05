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

    @Override
    public ClasseInfoDto validerTokenEtRecupererInfos(String token, String classId) {
        currentUser.requireAuthenticated();
        log.info("API - Validation du token et récupération des infos de classe");
        return parentAccessBusiness.validerTokenEtRecupererInfos(token, classId);
    }

    @Override
    public void traiterDemandeAcces(ParentAccessRequestDto request) {
        currentUser.requireSelfOrAdmin(request.getParentId());
        log.info("API - Traitement demande d'accès parent pour la classe {}", request.getClasseId());
        parentAccessBusiness.traiterDemandeAcces(request);
    }
}
