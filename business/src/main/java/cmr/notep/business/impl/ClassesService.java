package cmr.notep.business.impl;

import cmr.notep.business.business.ClassesBusiness;
import cmr.notep.interfaces.api.ClassesApi;
import cmr.notep.interfaces.modeles.Classes;
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

    @Override
    public Classes creerClasse(@NonNull Classes classes) {
        log.info("Tentative de création d'une nouvelle classe: {}", classes);
        Classes nouvelleClasse = classesBusiness.creerClasse(classes);
        log.info("Classe créée avec succès: {}", nouvelleClasse.getId());
        return nouvelleClasse;
    }


    @Override
    public Classes modifierClasse(@NonNull String idClasse, @NonNull Classes classeModifiee) {
        log.info("Tentative de modification de la classe avec l'ID: {}", idClasse);
        classeModifiee.setId(idClasse);
        Classes classeMAJ = classesBusiness.modifierClasse(idClasse, classeModifiee);
        log.info("Classe modifiée avec succès: {}", classeMAJ.getId());
        return classeMAJ;
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

}
