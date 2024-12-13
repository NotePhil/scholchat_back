package cmr.notep.business.impl;

import cmr.notep.business.business.ClassesBusiness;
import cmr.notep.interfaces.api.ClassesApi;
import cmr.notep.interfaces.modeles.Classes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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
        } catch (Exception e) {
            log.error("Erreur lors de la création de la classe", e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Impossible de créer la classe", e );
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
        } catch (RuntimeException e) {
            log.error("Erreur lors de la modification de la classe", e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Classe non trouvée ou impossible à modifier", e);
        }
    }

    @Override
    public void supprimerClasse(@NonNull String idClasse) {
        try {
            log.info("Tentative de suppression de la classe avec l'ID: {}", idClasse);
            classesBusiness.supprimerClasse(idClasse);
            log.info("Classe supprimée avec succès: {}", idClasse);
        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de la classe", e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,  // Changed from NOT_FOUND
                    "Classe non trouvée ou impossible à supprimer", e);
        }
    }

    @Override
    public Classes obtenirClasseParId(@NonNull String idClasse) {
        try {
            log.info("Récupération de la classe avec l'ID: {}", idClasse);
            Classes classe = classesBusiness.obtenirClasseParId(idClasse);
            log.info("Classe récupérée avec succès: {}", idClasse);
            return classe;
        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération de la classe", e);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Classe non trouvée", e );
        }
    }

    @Override
    public List<Classes> obtenirToutesLesClasses() {
        try {
            log.info("Récupération de toutes les classes");
            List<Classes> classes = classesBusiness.obtenirToutesLesClasses();
            log.info("Récupération de {} classes", classes.size());
            return classes;
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de toutes les classes", e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Impossible de récupérer les classes", e );
        }
    }
}
