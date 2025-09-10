package cmr.notep.business.impl;

import cmr.notep.business.business.MotifsRejetBusiness;
import cmr.notep.interfaces.api.MotifsRejetApi;
import cmr.notep.interfaces.modeles.MotifRejet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class MotifsRejetService implements MotifsRejetApi {
    private final MotifsRejetBusiness motifsRejetBusiness;

    public MotifsRejetService(MotifsRejetBusiness motifsRejetBusiness) {
        this.motifsRejetBusiness = motifsRejetBusiness;
    }

    @Override
    public MotifRejet creerMotifRejet(@RequestBody MotifRejet motifRejet) {
        return motifsRejetBusiness.creerMotifRejet(motifRejet);
    }

    @Override
    public List<MotifRejet> obtenirTousMotifsRejet() {
        return motifsRejetBusiness.obtenirTousMotifsRejet();
    }

    @Override
    public MotifRejet obtenirMotifParId(@PathVariable String id) {
        return motifsRejetBusiness.obtenirMotifParId(id);
    }

    @Override
    public MotifRejet modifierMotifRejet(@PathVariable String id, @RequestBody MotifRejet motifRejet) {
        return motifsRejetBusiness.modifierMotifRejet(id, motifRejet);
    }

    @Override
    public void supprimerMotifRejet(@PathVariable String id) {
        motifsRejetBusiness.supprimerMotifRejet(id);
    }
}