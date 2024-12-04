package cmr.notep.business.impl;

import cmr.notep.business.business.ProfesseursBusiness;
import cmr.notep.interfaces.api.ProfesseursApi;
import cmr.notep.interfaces.modeles.Professeurs;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ProfesseursService implements ProfesseursApi {

    private final ProfesseursBusiness professeursBusiness;

    public ProfesseursService(ProfesseursBusiness professeursBusiness) {
        this.professeursBusiness = professeursBusiness;
    }

    @Override
    public Professeurs obtenirProfesseurParId(@NonNull String idProfesseur) {
        log.info("Fetching professor by ID: {}", idProfesseur);
        return professeursBusiness.avoirProfesseur(idProfesseur);
    }

    @Override
    public List<Professeurs> obtenirTousLesProfesseurs() {
        log.info("Fetching all professors...");
        return professeursBusiness.avoirTousProfesseur();
    }

    @Override
    public Professeurs creerProfesseur(@NonNull Professeurs professeur) {
        log.info("Creating professor: {}", professeur);
        return professeursBusiness.creerProfesseur(professeur);
    }
}
