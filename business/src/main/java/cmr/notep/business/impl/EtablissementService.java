package cmr.notep.business.impl;

import cmr.notep.business.business.EtablissementBusiness;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.utils.ExceptionUtil;
import cmr.notep.interfaces.api.EtablissementApi;
import cmr.notep.interfaces.modeles.Etablissement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class EtablissementService implements EtablissementApi {
    private final EtablissementBusiness etablissementBusiness;

    @Override
    public Etablissement creerEtablissement(Etablissement etablissement) {
        try {
            log.info("Création d'un nouvel établissement: {}", etablissement.getNom());
            return etablissementBusiness.creerEtablissement(etablissement);
        } catch (SchoolException e) {
            throw ExceptionUtil.toResponseStatusException(e);
        }
    }

    @Override
    public Etablissement modifierEtablissement(String idEtablissement, Etablissement etablissementModifie) {
        try {
            log.info("Modification de l'établissement avec ID: {}", idEtablissement);
            return etablissementBusiness.modifierEtablissement(idEtablissement, etablissementModifie);
        } catch (SchoolException e) {
            log.error("Erreur lors de la modification", e);
            throw ExceptionUtil.toResponseStatusException(e);
        }
    }

    @Override
    public void supprimerEtablissement(String idEtablissement) {
        log.info("Suppression de l'établissement avec ID: {}", idEtablissement);
        try {
            etablissementBusiness.supprimerEtablissement(idEtablissement);
        } catch (SchoolException e) {
            throw ExceptionUtil.toResponseStatusException(e);
        }
    }

    @Override
    public Etablissement obtenirEtablissementParId(String idEtablissement) {
        log.info("Récupération de l'établissement avec ID: {}", idEtablissement);
        try {
            return etablissementBusiness.obtenirEtablissementParId(idEtablissement);
        } catch (SchoolException e) {
            throw ExceptionUtil.toResponseStatusException(e);
        }
    }

    @Override
    public List<Etablissement> obtenirTousLesEtablissements() {
        log.info("Récupération de tous les établissements");
        return etablissementBusiness.obtenirTousLesEtablissements();
    }
}
