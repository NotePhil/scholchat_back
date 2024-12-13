package cmr.notep.business.impl;

import cmr.notep.business.business.EtablissementBusiness;
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
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'établissement", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur lors de la création", e);
        }
    }

    @Override
    public Etablissement modifierEtablissement(String idEtablissement, Etablissement etablissementModifie) {
        try {
            log.info("Modification de l'établissement avec ID: {}", idEtablissement);
            return etablissementBusiness.modifierEtablissement(idEtablissement, etablissementModifie);
        } catch (Exception e) {
            log.error("Erreur lors de la modification", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Etablissement non trouvé ou erreur", e);
        }
    }

    @Override
    public void supprimerEtablissement(String idEtablissement) {
        try {
            log.info("Suppression de l'établissement avec ID: {}", idEtablissement);
            etablissementBusiness.supprimerEtablissement(idEtablissement);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Etablissement non trouvé", e);
        }
    }

    @Override
    public Etablissement obtenirEtablissementParId(String idEtablissement) {
        try {
            log.info("Récupération de l'établissement avec ID: {}", idEtablissement);
            return etablissementBusiness.obtenirEtablissementParId(idEtablissement);
        } catch (Exception e) {
            log.error("Etablissement non trouvé", e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Etablissement non trouvé", e);
        }
    }

    @Override
    public List<Etablissement> obtenirTousLesEtablissements() {
        log.info("Récupération de tous les établissements");
        return etablissementBusiness.obtenirTousLesEtablissements();
    }
}
