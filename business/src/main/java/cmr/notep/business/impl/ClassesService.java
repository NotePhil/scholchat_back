package cmr.notep.business.impl;

import cmr.notep.business.business.ClassesBusiness;
import cmr.notep.business.business.HistoActivationBusiness;
import cmr.notep.interfaces.api.ClassesApi;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.HistoActivation;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.modele.DroitPublication;
import cmr.notep.modele.EtatClasse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClassesService implements ClassesApi {

    private final ClassesBusiness classesBusiness;
    private final HistoActivationBusiness histoActivationBusiness;

    @Override
    public Classes creerClasse(@NonNull Classes classes) {
        log.info("Tentative de création d'une nouvelle classe: {}", classes);
        Classes nouvelleClasse = classesBusiness.creerClasse(classes);
        log.info("Classe créée avec succès: {}", nouvelleClasse.getId());
        return nouvelleClasse;
    }

//    @Override
//    public List<Utilisateurs> obtenirUtilisateursParClasse(String idClasse) {
//        log.info("Récupération des utilisateurs pour la classe avec l'ID: {}", idClasse);
//        List<Utilisateurs> utilisateurs = classesBusiness.obtenirUtilisateursParClasse(idClasse);
//        log.info("Récupération de {} utilisateurs pour la classe avec l'ID: {}", utilisateurs.size(), idClasse);
//        return utilisateurs;
//    }

    @Override
    public Classes modifierClasse(@NonNull String idClasse, @NonNull Classes classeModifiee) {
        log.info("Tentative de modification de la classe avec l'ID: {}", idClasse);
        classeModifiee.setId(idClasse);
        Classes classeMAJ = classesBusiness.modifierClasse(idClasse, classeModifiee);
        log.info("Classe modifiée avec succès: {}", classeMAJ.getId());
        return classeMAJ;
    }

    @Override
    public Classes approuverClasse(@NonNull String idClasse) {
        log.info("Approbation de la classe avec l'ID: {}", idClasse);
        Classes classeApprouvee = classesBusiness.approuverClasse(idClasse);
        log.info("Classe approuvée avec succès: {}", idClasse);
        return classeApprouvee;
    }

    @Override
    public Classes rejeterClasse(@NonNull String idClasse, @NonNull String motif) {
        log.info("Rejet de la classe avec l'ID: {}", idClasse);
        Classes classeRejetee = classesBusiness.rejeterClasse(idClasse, motif);
        log.info("Classe rejetée avec succès: {}", idClasse);
        return classeRejetee;
    }

    @Override
    public List<Classes> obtenirClassesParEtat(@NonNull EtatClasse etat) {
        log.info("Récupération des classes avec l'état: {}", etat);
        List<Classes> classes = classesBusiness.obtenirClassesParEtat(etat);
        log.info("Récupération de {} classes avec l'état {}", classes.size(), etat);
        return classes;
    }

    @Override
    public void supprimerClasse(@NonNull String idClasse) {
        log.info("Tentative de suppression de la classe avec l'ID: {}", idClasse);
        classesBusiness.supprimerClasse(idClasse);
        log.info("Classe supprimée avec succès: {}", idClasse);
    }

    @Override
    public Classes obtenirClasseParId(@NonNull String idClasse) {
        log.info("Récupération de la classe avec l'ID: {}", idClasse);
        return classesBusiness.obtenirClasseParId(idClasse);
    }

    @Override
    public List<Classes> obtenirToutesLesClasses() {
        log.info("Récupération de toutes les classes");
        List<Classes> classes = classesBusiness.obtenirToutesLesClasses();
        log.info("Récupération de {} classes", classes.size());
        return classes;
    }

    @Override
    public Classes modifierDroitPublication(String idClasse, DroitPublication droitPublication) {
        log.info("Modification du droit de publication pour la classe: {}", idClasse);
        return classesBusiness.modifierDroitPublication(idClasse, droitPublication);
    }

    @Override
    public List<HistoActivation> obtenirHistoriqueActivation(String idClasse) {
        log.info("Récupération de l'historique d'activation pour la classe: {}", idClasse);
        return histoActivationBusiness.obtenirHistoriqueParClasse(idClasse);
    }

    @Override
    public Classes assignerModerator(@NonNull String idClasse, @NonNull String idModerator) {
        log.info("Attribution du modérateur {} à la classe {}", idModerator, idClasse);
        Classes classeMAJ = classesBusiness.assignerModerator(idClasse, idModerator);
        log.info("Modérateur assigné avec succès à la classe: {}", idClasse);
        return classeMAJ;
    }

    @Override
    public Classes retirerModerator(@NonNull String idClasse) {
        log.info("Retrait du modérateur de la classe: {}", idClasse);
        Classes classeMAJ = classesBusiness.retirerModerator(idClasse);
        log.info("Modérateur retiré avec succès de la classe: {}", idClasse);
        return classeMAJ;
    }

    @Override
    public List<Utilisateurs> obtenirModerateursDeLaClasse(@NonNull String idClasse) {
        log.info("Récupération des modérateurs de la classe: {}", idClasse);
        List<Utilisateurs> moderateurs = classesBusiness.obtenirModerateursDeLaClasse(idClasse);
        log.info("Récupération de {} modérateurs pour la classe: {}", moderateurs.size(), idClasse);
        return moderateurs;
    }
}