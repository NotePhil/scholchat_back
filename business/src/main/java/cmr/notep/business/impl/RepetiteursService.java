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
    public Repetiteurs obtenirRepetiteurParId(@NonNull Long idRepetiteur) {
        log.info("Fetching répétiteur by ID: {}", idRepetiteur);
        return repetiteursBusiness.obtenirRepetiteurParId(idRepetiteur);
    }

    @Override
    public List<Repetiteurs> obtenirTousLesRepetiteurs() {
        log.info("Fetching all répétiteurs...");
        return repetiteursBusiness.obtenirTousLesRepetiteurs();
    }

    @Override
    public Repetiteurs creerRepetiteur(@NonNull Repetiteurs repetiteur) {
        log.info("Creating répétiteur: {}", repetiteur);
        return repetiteursBusiness.creerRepetiteur(repetiteur);
    }
}
