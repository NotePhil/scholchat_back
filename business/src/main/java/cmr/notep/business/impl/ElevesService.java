package cmr.notep.business.impl;

import cmr.notep.business.business.ElevesBusiness;
import cmr.notep.interfaces.api.ElevesApi;
import cmr.notep.interfaces.modeles.Eleves;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ElevesService implements ElevesApi {
    private final ElevesBusiness elevesBusiness;

    public ElevesService(ElevesBusiness elevesBusiness) {
        this.elevesBusiness = elevesBusiness;
    }

    @Override
    public Eleves avoirEleve(@NonNull String idEleve) {
        return elevesBusiness.avoirEleve(idEleve);
    }

    @Override
    public List<Eleves> avoirToutEleves() {
        return elevesBusiness.avoirToutEleves();
    }

    @Override
    public Eleves posterEleve(@NonNull Eleves Eleve) {
        return elevesBusiness.posterEleve(Eleve);
    }
}
