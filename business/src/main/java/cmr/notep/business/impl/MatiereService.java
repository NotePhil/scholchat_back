package cmr.notep.business.impl;

import cmr.notep.business.business.MatiereBusiness;
import cmr.notep.interfaces.api.MatiereApi;
import cmr.notep.interfaces.modeles.Matiere;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class MatiereService implements MatiereApi {

    private final MatiereBusiness matiereBusiness;

    public MatiereService(MatiereBusiness matiereBusiness) {
        this.matiereBusiness = matiereBusiness;
    }

    @Override
    public Matiere creerMatiere(@NonNull Matiere matiere) {
        log.info("Création d'une nouvelle matière: {}", matiere.getNom());
        return matiereBusiness.creerMatiere(matiere);
    }

    @Override
    public List<Matiere> obtenirToutesMatieres() {
        log.info("Récupération de toutes les matières");
        return matiereBusiness.obtenirToutesMatieres();
    }

    @Override
    public Matiere obtenirMatiereParNom(@NonNull String nomMatiere) {
        log.info("Récupération de la matière: {}", nomMatiere);
        return matiereBusiness.obtenirMatiereParNom(nomMatiere);
    }

    @Override
    public Matiere modifierMatiere(@NonNull String id, @NonNull Matiere matiere) {
        log.info("Modification de la matière avec l'ID: {}", id);
        return matiereBusiness.modifierMatiere(id, matiere);
    }

    @Override
    public void supprimerMatiere(@NonNull String id) {
        log.info("Suppression de la matière avec l'ID: {}", id);
        matiereBusiness.supprimerMatiere(id);
    }
}