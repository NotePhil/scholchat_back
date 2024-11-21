package cmr.notep.business.impl;

import cmr.notep.business.business.ProfesseurBusiness;
import cmr.notep.interfaces.api.ProfesseurApi;
import cmr.notep.interfaces.modeles.Professeur;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class ProfesseursService implements ProfesseurApi {

    private final ProfesseurBusiness professeurBusiness;

    public ProfesseursService(ProfesseurBusiness professeurBusiness) {
        this.professeurBusiness = professeurBusiness;
    }

    @Override
    public Professeur obtenirProfesseurParId(@NonNull Long idProfesseur) {
        log.info("Fetching professor by ID: {}", idProfesseur);
        return professeurBusiness.obtenirProfesseurParId(idProfesseur);
    }

    @Override
    public List<Professeur> obtenirTousLesProfesseurs() {
        log.info("Fetching all professors...");
        return professeurBusiness.obtenirTousLesProfesseurs();
    }

    @Override
    public Professeur creerProfesseur(@NonNull Professeur professeur) {
        log.info("Creating professor: {}", professeur);
        return professeurBusiness.creerProfesseur(professeur);
    }
}
