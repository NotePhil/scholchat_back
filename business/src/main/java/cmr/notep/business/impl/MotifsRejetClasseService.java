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
    private final MotifsRejetClasseBusiness motifsRejetClasseBusiness;

    @Override
    public MotifRejetClasse creerMotifRejetClasse(MotifRejetClasse motifRejetClasse) {
        return motifsRejetClasseBusiness.creerMotifRejetClasse(motifRejetClasse);
    }

    @Override
    public List<MotifRejetClasse> obtenirTousMotifsRejetClasse() {
        return motifsRejetClasseBusiness.obtenirTousMotifsRejetClasse();
    }

    @Override
    public void supprimerMotifRejetClasse(String id) {
        motifsRejetClasseBusiness.supprimerMotifRejetClasse(id);
    }
}