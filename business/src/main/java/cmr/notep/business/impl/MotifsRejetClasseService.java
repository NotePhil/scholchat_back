package cmr.notep.business.impl;

import cmr.notep.business.business.MotifsRejetClasseBusiness;
import cmr.notep.interfaces.api.MotifsRejetClasseApi;
import cmr.notep.interfaces.modeles.MotifRejetClasse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MotifsRejetClasseService implements MotifsRejetClasseApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;

    private final MotifsRejetClasseBusiness motifsRejetClasseBusiness;

    @Override
    public MotifRejetClasse creerMotifRejetClasse(MotifRejetClasse motifRejetClasse) {
        currentUser.requireAdmin();
        return motifsRejetClasseBusiness.creerMotifRejetClasse(motifRejetClasse);
    }

    @Override
    public List<MotifRejetClasse> obtenirTousMotifsRejetClasse() {
        return motifsRejetClasseBusiness.obtenirTousMotifsRejetClasse();
    }

    @Override
    public void supprimerMotifRejetClasse(String id) {
        currentUser.requireAdmin();
        motifsRejetClasseBusiness.supprimerMotifRejetClasse(id);
    }

    @Override
    public MotifRejetClasse obtenirMotifClasseParCode(String code) {
        return motifsRejetClasseBusiness.obtenirMotifClasseParCode(code);
    }
}