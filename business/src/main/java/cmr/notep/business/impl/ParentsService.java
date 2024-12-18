package cmr.notep.business.impl;

import cmr.notep.business.business.ParentsBusiness;
import cmr.notep.interfaces.api.ParentsApi;
import cmr.notep.interfaces.modeles.Parents;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ParentsService implements ParentsApi {
    private final ParentsBusiness parentsBusiness;

    public ParentsService(ParentsBusiness ParentsBusiness) {
        this.parentsBusiness = ParentsBusiness;
    }

    @Override
    public Parents avoirParent(@NonNull String idProfilParent) {
        return parentsBusiness.avoirParent(idProfilParent);
    }

    @Override
    public List<Parents> avoirToutParents() {
        return parentsBusiness.avoirToutParents();
    }

    @Override
    public Parents posterParent(@NonNull Parents profilParent) {
        return parentsBusiness.posterParent(profilParent);
    }
}