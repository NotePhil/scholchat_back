package cmr.notep.business.impl;

import cmr.notep.business.business.ProfilParentsBusiness;
import cmr.notep.interfaces.api.ProfilParentsApi;
import cmr.notep.interfaces.modeles.ProfilParents;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ProfilParentsService implements ProfilParentsApi {
    private final ProfilParentsBusiness profilParentsBusiness;

    public ProfilParentsService(ProfilParentsBusiness profilParentsBusiness) {
        this.profilParentsBusiness = profilParentsBusiness;
    }

    @Override
    public ProfilParents avoirProfilParent(@NonNull String idProfilParent) {
        return profilParentsBusiness.avoirProfilParent(idProfilParent);
    }

    @Override
    public List<ProfilParents> avoirToutProfilParents() {
        return profilParentsBusiness.avoirToutProfilParents();
    }

    @Override
    public ProfilParents posterProfilParent(@NonNull ProfilParents profilParent) {
        return profilParentsBusiness.posterProfilParent(profilParent);
    }
}