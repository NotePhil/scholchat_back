package cmr.notep.business.impl;

import cmr.notep.business.business.EtablissementBusiness;
import cmr.notep.interfaces.api.EtablissementApi;
import cmr.notep.interfaces.modeles.Etablissement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j

public class EtablissementService implements EtablissementApi {
    private final EtablissementBusiness etablissementBusiness;

    public EtablissementService(EtablissementBusiness etablissementBusiness) {
        this.etablissementBusiness = etablissementBusiness;
    }

    @Override
    public Etablissement creerEtablissement(Etablissement etablissement) {
            log.info("Création d'un nouvel établissement: {}", etablissement.getNom());
            return etablissementBusiness.creerEtablissement(etablissement);
    }

    @Override
    public Etablissement modifierEtablissement(String idEtablissement, Etablissement etablissementModifie) {
            log.info("Modification de l'établissement avec ID: {}", idEtablissement);
            return etablissementBusiness.modifierEtablissement(idEtablissement, etablissementModifie);
    }

    @Override
    public void supprimerEtablissement(String idEtablissement) {
        log.info("Suppression de l'établissement avec ID: {}", idEtablissement);
            etablissementBusiness.supprimerEtablissement(idEtablissement);
    }

    @Override
    public Etablissement obtenirEtablissementParId(String idEtablissement) {
        log.info("Récupération de l'établissement avec ID: {}", idEtablissement);
            return etablissementBusiness.obtenirEtablissementParId(idEtablissement);
    }

    @Override
    public List<Etablissement> obtenirTousLesEtablissements() {
        log.info("Récupération de tous les établissements");
        return etablissementBusiness.obtenirTousLesEtablissements();
    }
}
