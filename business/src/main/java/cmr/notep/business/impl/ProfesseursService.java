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
    public Professeurs avoirProfesseur(@NonNull String idProfesseur) {
        return professeursBusiness.avoirProfesseur(idProfesseur);
    }

    @Override
    public List<Professeurs> avoirToutProfesseurs() {
        return professeursBusiness.avoirToutProfesseurs();
    }

    @Override
    public Professeurs posterProfesseur(@NonNull Professeurs professeur) {
        return professeursBusiness.posterProfesseur(professeur);
    }

    @Override
    public Professeurs avoirProfesseurParMatricule(@NonNull String matriculeProfesseur) {
        return professeursBusiness.avoirProfesseurParMatricule(matriculeProfesseur);
    }
}