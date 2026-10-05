package cmr.notep.business.impl;

import cmr.notep.business.business.CoursBusiness;
import cmr.notep.interfaces.api.CoursApi;
import cmr.notep.interfaces.dto.CoursProgressionDTO;
import cmr.notep.interfaces.modeles.Cours;
import cmr.notep.modele.EtatCours;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class CoursService implements CoursApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;


    private final CoursBusiness coursBusiness;

    public CoursService(CoursBusiness coursBusiness) {
        this.coursBusiness = coursBusiness;
    }

    @Override
    public Cours creerCours(@NonNull Cours cours) {
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin()) {
            if (!currentUser.hasRole("PROFESSOR") && !currentUser.hasRole("TUTOR")) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("Seuls les professeurs peuvent créer du contenu pédagogique.");
            }
            cours.setRedacteurId(currentUser.requireUserId()); // un professeur ne crée que pour lui-même
        }
        log.info("Création d'un nouveau cours: {}", cours.getTitre());
        return coursBusiness.creerCours(cours);
    }

    @Override
    public List<Cours> obtenirCoursParProfesseur(@NonNull String professeurId) {
        log.info("Récupération des cours pour le professeur: {}", professeurId);
        return coursBusiness.obtenirCoursParProfesseur(professeurId);
    }

    @Override
    public List<Cours> obtenirCoursParEtat(@NonNull EtatCours etat) {
        log.info("Récupération des cours avec l'état: {}", etat);
        return coursBusiness.obtenirCoursParEtat(etat);
    }

    @Override
    public List<Cours> obtenirCoursAccessibles(@NonNull String userId) {
        accessControl.requireSelfOrParentOrAdmin(userId);
        log.info("Récupération des cours accessibles pour l'utilisateur: {}", userId);
        return coursBusiness.obtenirCoursAccessibles(userId);
    }
    @Override
    public Cours mettreAJourCours(@NonNull String coursId, @NonNull Cours cours) {
        accessControl.requireCoursAuthor(coursId);
        if (!currentUser.isAdmin()) cours.setRedacteurId(null); // l'auteur ne peut pas être réattribué
        log.info("Mise à jour du cours: {}", coursId);
        return coursBusiness.mettreAJourCours(coursId, cours);
    }

    @Override
    public void supprimerCours(@NonNull String coursId) {
        accessControl.requireCoursAuthor(coursId);
        log.info("Suppression du cours: {}", coursId);
        coursBusiness.supprimerCours(coursId);
    }

    @Override
    public Cours obtenirCoursParId(@NonNull String coursId) {
        log.info("Récupération du cours: {}", coursId);
        return coursBusiness.obtenirCoursParId(coursId);
    }

    @Override
    public void marquerChapitreComplete(@NonNull String coursId, @NonNull String chapitreId, @NonNull String utilisateurId) {
        currentUser.requireSelfOrAdmin(utilisateurId);
        log.info("Chapitre {} marqué complété par {}", chapitreId, utilisateurId);
        coursBusiness.marquerChapitreComplete(utilisateurId, chapitreId);
    }

    @Override
    public CoursProgressionDTO obtenirProgression(@NonNull String coursId, @NonNull String utilisateurId) {
        if (!currentUser.isAdmin() && !currentUser.isSelf(utilisateurId) && !accessControl.isParentOf(currentUser.requireUserId(), utilisateurId)) {
            accessControl.requireCoursAuthor(coursId);
        }
        log.info("Progression du cours {} pour l'utilisateur {}", coursId, utilisateurId);
        return coursBusiness.obtenirProgression(utilisateurId, coursId);
    }

    @Override
    public List<Cours> obtenirCoursParRestriction(@NonNull String restriction) {
        log.info("Récupération des cours avec restriction: {}", restriction);
        return coursBusiness.obtenirCoursParRestriction(restriction);
    }
    @Override
    public Cours obtenirCoursAvecChapitres(@NonNull String coursId) {
        log.info("Récupération du cours avec chapitres: {}", coursId);
        return coursBusiness.obtenirCoursAvecChapitres(coursId);
    }
}