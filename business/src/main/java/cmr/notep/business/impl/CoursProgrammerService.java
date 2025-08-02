package cmr.notep.business.impl;

import cmr.notep.business.business.CoursProgrammerBusiness;
import cmr.notep.interfaces.api.CoursProgrammerApi;
import cmr.notep.interfaces.modeles.CoursProgrammer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CoursProgrammerService implements CoursProgrammerApi {

    private final CoursProgrammerBusiness coursProgrammerBusiness;

    @Override
    public CoursProgrammer programmerCours(CoursProgrammer coursProgrammer) {
        return coursProgrammerBusiness.programmerCours(coursProgrammer);
    }

    @Override
    public List<CoursProgrammer> obtenirProgrammationParCours(String coursId) {
        return coursProgrammerBusiness.obtenirProgrammationParCours(coursId);
    }

    @Override
    public List<CoursProgrammer> obtenirProgrammationParClasse(String classeId) {
        return coursProgrammerBusiness.obtenirProgrammationParClasse(classeId);
    }
}