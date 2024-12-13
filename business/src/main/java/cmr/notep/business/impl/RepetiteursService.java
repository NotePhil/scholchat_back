package cmr.notep.business.impl;

import cmr.notep.business.business.RepetiteursBusiness;
import cmr.notep.interfaces.api.RepetiteursApi;
import cmr.notep.interfaces.modeles.Repetiteurs;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class RepetiteursService implements RepetiteursApi {
    private final RepetiteursBusiness repetiteursBusiness;

    public RepetiteursService(RepetiteursBusiness repetiteursBusiness) {
        this.repetiteursBusiness = repetiteursBusiness;
    }

    @Override
    public Repetiteurs avoirRepetiteur(@NonNull String idRepetiteur) {
        return repetiteursBusiness.avoirRepetiteur(idRepetiteur);
    }

    @Override
    public List<Repetiteurs> avoirToutRepetiteurs() {
        return repetiteursBusiness.avoirToutRepetiteurs();
    }

    @Override
    public Repetiteurs posterRepetiteur(@NonNull Repetiteurs repetiteur) {
        return repetiteursBusiness.posterRepetiteur(repetiteur);
    }
}
