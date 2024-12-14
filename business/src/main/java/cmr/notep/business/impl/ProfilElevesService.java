package cmr.notep.business.impl;

import cmr.notep.business.business.ProfilElevesBusiness;
import cmr.notep.interfaces.api.ProfilElevesApi;
import cmr.notep.interfaces.modeles.ProfilEleves;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ProfilElevesService implements ProfilElevesApi {
    private final ProfilElevesBusiness profilElevesBusiness;

    public ProfilElevesService(ProfilElevesBusiness profilElevesBusiness) {
        this.profilElevesBusiness = profilElevesBusiness;
    }

    @Override
    public ProfilEleves avoirProfilEleve(@NonNull String idProfilEleve) {
        return profilElevesBusiness.avoirProfilEleve(idProfilEleve);
    }

    @Override
    public List<ProfilEleves> avoirToutProfilEleves() {
        return profilElevesBusiness.avoirToutProfilEleves();
    }

    @Override
    public ProfilEleves posterProfilEleve(@NonNull ProfilEleves profilEleve) {
        return profilElevesBusiness.posterProfilEleve(profilEleve);
    }
}
