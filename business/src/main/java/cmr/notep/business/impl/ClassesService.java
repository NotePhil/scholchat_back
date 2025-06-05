package cmr.notep.business.impl;

import cmr.notep.business.business.ClassesBusiness;
import cmr.notep.interfaces.api.ClassesApi;
import cmr.notep.interfaces.modeles.Classes;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClassesService implements ClassesApi {

    private final ClassesBusiness classesBusiness;

    @Override
    public Classes creerClasse(@NonNull @RequestBody Classes classes) {
        String professeurId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Tentative de création d'une nouvelle classe par le professeur: {}", professeurId);
        Classes nouvelleClasse = classesBusiness.creerClasse(classes, professeurId);
        log.info("Classe créée avec succès: {}", nouvelleClasse.getId());
        return nouvelleClasse;
    }

    @Override
    public Classes modifierClasse(@NonNull @PathVariable("idClasse") String idClasse,
                                  @NonNull @RequestBody Classes classeModifiee) {
        String professeurId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Tentative de modification de la classe avec l'ID: {} par le professeur: {}", idClasse, professeurId);
        classeModifiee.setId(idClasse);
        Classes classeMAJ = classesBusiness.modifierClasse(idClasse, classeModifiee, professeurId);
        log.info("Classe modifiée avec succès: {}", classeMAJ.getId());
        return classeMAJ;
    }

    @Override
    public void supprimerClasse(@NonNull @PathVariable("idClasse") String idClasse) {
        String professeurId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Tentative de suppression de la classe avec l'ID: {} par le professeur: {}", idClasse, professeurId);
        classesBusiness.supprimerClasse(idClasse, professeurId);
        log.info("Classe supprimée avec succès: {}", idClasse);
    }

    @Override
    public Classes obtenirClasseParId(@NonNull @PathVariable("idClasse") String idClasse) {
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

    @PostMapping("/{idClasse}/eleves/{idEleve}")
    public Classes ajouterEleve(@PathVariable String idClasse, @PathVariable String idEleve) {
        String professeurId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Ajout de l'élève {} à la classe {} par le professeur {}", idEleve, idClasse, professeurId);
        return classesBusiness.ajouterEleve(idClasse, idEleve, professeurId);
    }

    @DeleteMapping("/{idClasse}/eleves/{idEleve}")
    public Classes supprimerEleve(@PathVariable String idClasse, @PathVariable String idEleve) {
        String professeurId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Suppression de l'élève {} de la classe {} par le professeur {}", idEleve, idClasse, professeurId);
        return classesBusiness.supprimerEleve(idClasse, idEleve, professeurId);
    }

    @PostMapping("/{idClasse}/parents/{idParent}")
    public Classes ajouterParent(@PathVariable String idClasse, @PathVariable String idParent) {
        String professeurId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Ajout du parent {} à la classe {} par le professeur {}", idParent, idClasse, professeurId);
        return classesBusiness.ajouterParent(idClasse, idParent, professeurId);
    }

    @DeleteMapping("/{idClasse}/parents/{idParent}")
    public Classes supprimerParent(@PathVariable String idClasse, @PathVariable String idParent) {
        String professeurId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Suppression du parent {} de la classe {} par le professeur {}", idParent, idClasse, professeurId);
        return classesBusiness.supprimerParent(idClasse, idParent, professeurId);
    }
}