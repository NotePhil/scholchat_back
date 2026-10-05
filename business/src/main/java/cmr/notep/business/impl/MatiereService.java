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
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;


    private final MatiereBusiness matiereBusiness;

    public MatiereService(MatiereBusiness matiereBusiness) {
        this.matiereBusiness = matiereBusiness;
    }

    @Override
    public Matiere creerMatiere(@NonNull Matiere matiere) {
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin() && !currentUser.hasRole("PROFESSOR")) throw cmr.notep.business.security.CurrentUserService.forbidden("Seuls les professeurs et administrateurs peuvent créer une matière.");
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
        currentUser.requireAdmin();
        log.info("Modification de la matière avec l'ID: {}", id);
        return matiereBusiness.modifierMatiere(id, matiere);
    }

    @Override
    public void supprimerMatiere(@NonNull String id) {
        currentUser.requireAdmin();
        log.info("Suppression de la matière avec l'ID: {}", id);
        matiereBusiness.supprimerMatiere(id);
    }
}