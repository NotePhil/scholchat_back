package cmr.notep.business.impl;

import cmr.notep.business.business.ClassesBusiness;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.interfaces.api.ClassesApi;
import cmr.notep.interfaces.modeles.Classes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestController;
import cmr.notep.business.utils.ExceptionUtil;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClassesService implements ClassesApi {

    private final ClassesBusiness classesBusiness;

    @Override
    public Classes creerClasse(@NonNull Classes classes) {
        try {
            log.info("Tentative de création d'une nouvelle classe: {}", classes);
            Classes nouvelleClasse = classesBusiness.creerClasse(classes);
            log.info("Classe créée avec succès: {}", nouvelleClasse.getId());
            return nouvelleClasse;
        } catch (SchoolException e) {
            log.error("Erreur lors de la création de la classe", e);
            throw ExceptionUtil.toResponseStatusException(e);
        }
    }

    @Override
    public Classes modifierClasse(@NonNull String idClasse, @NonNull Classes classeModifiee) {
        try {
            log.info("Tentative de modification de la classe avec l'ID: {}", idClasse);

            // Ensure the ID is set in the classeModifiee object
            classeModifiee.setId(idClasse);
            Classes classeMAJ = classesBusiness.modifierClasse(idClasse, classeModifiee);
            log.info("Classe modifiée avec succès: {}", classeMAJ.getId());
            return classeMAJ;
        } catch (SchoolException e) {
            log.error("Erreur lors de la modification de la classe", e);
            throw ExceptionUtil.toResponseStatusException(e);
        }
    }

    @Override
    public void supprimerClasse(@NonNull String idClasse) {
        try {
            log.info("Tentative de suppression de la classe avec l'ID: {}", idClasse);
            classesBusiness.supprimerClasse(idClasse);
            log.info("Classe supprimée avec succès: {}", idClasse);
        } catch (SchoolException e) {
            log.error("Erreur lors de la suppression de la classe", e);
            throw ExceptionUtil.toResponseStatusException(e);
        }
    }

    @Override
    public Classes obtenirClasseParId(@NonNull String idClasse) {
        try {
            log.info("Récupération de la classe avec l'ID: {}", idClasse);
            return classesBusiness.obtenirClasseParId(idClasse);
        } catch (SchoolException e) {
            log.error("Unexpected exception: {}", e.getMessage(), e);
            throw ExceptionUtil.toResponseStatusException(e);
        }
    }


    @Override
    public List<Classes> obtenirToutesLesClasses() {
        try {
            log.info("Récupération de toutes les classes");
            List<Classes> classes = classesBusiness.obtenirToutesLesClasses();
            log.info("Récupération de {} classes", classes.size());
            return classes;
        } catch (SchoolException e) {
            log.error("Erreur lors de la récupération de toutes les classes", e);
            throw ExceptionUtil.toResponseStatusException(e);
        }
    }

}
