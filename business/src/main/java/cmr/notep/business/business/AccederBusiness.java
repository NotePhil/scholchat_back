package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.AccederRepository;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class AccederBusiness {

    private final DaoAccessorService daoAccessorService;

    public AccederBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public void donnerAcces(String utilisateurId, String classeId) throws SchoolException {
        log.info("Donner accès à l'utilisateur {} à la classe {}", utilisateurId, classeId);

        // Check if access already exists
        if (daoAccessorService.getRepository(AccederRepository.class)
                .existsByUtilisateurIdAndClasseId(utilisateurId, classeId)) {
            throw new SchoolException(SchoolErrorCode.ALREADY_EXISTS,
                    "L'utilisateur a déjà accès à cette classe");
        }

        // Get user and class
        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(utilisateurId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable"));

        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                .findById(classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable"));

        // Create new access
        AccederEntity acceder = new AccederEntity();
        acceder.setUtilisateurId(utilisateurId);
        acceder.setClasseId(classeId);
        acceder.setUtilisateur(utilisateur);
        acceder.setClasse(classe);
        acceder.setDateAcces(new Date());

        // Save
        daoAccessorService.getRepository(AccederRepository.class).save(acceder);
        log.info("Accès accordé avec succès");
    }

    public void retirerAcces(String utilisateurId, String classeId) throws SchoolException {
        log.info("Retirer l'accès de l'utilisateur {} à la classe {}", utilisateurId, classeId);

        // Find the access record
        AccederEntity acceder = daoAccessorService.getRepository(AccederRepository.class)
                .findByUtilisateurIdAndClasseId(utilisateurId, classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "L'utilisateur n'a pas accès à cette classe"));

        // Delete access
        daoAccessorService.getRepository(AccederRepository.class).delete(acceder);
        log.info("Accès retiré avec succès");
    }

    public List<Utilisateurs> obtenirUtilisateursAvecAcces(String classeId) throws SchoolException {
        log.info("Obtenir tous les utilisateurs ayant accès à la classe {}", classeId);

        // Verify class exists
        if (!daoAccessorService.getRepository(ClassesRepository.class).existsById(classeId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable");
        }

        return daoAccessorService.getRepository(AccederRepository.class)
                .findByClasseId(classeId)
                .stream()
                .map(acceder -> dozerMapperBean.map(acceder.getUtilisateur(), Utilisateurs.class))
                .collect(Collectors.toList());
    }

    public List<Classes> obtenirClassesAccessibles(String utilisateurId) throws SchoolException {
        log.info("Obtenir toutes les classes accessibles par l'utilisateur {}", utilisateurId);

        // Verify user exists
        if (!daoAccessorService.getRepository(UtilisateursRepository.class).existsById(utilisateurId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable");
        }

        return daoAccessorService.getRepository(AccederRepository.class)
                .findByUtilisateurId(utilisateurId)
                .stream()
                .map(acceder -> dozerMapperBean.map(acceder.getClasse(), Classes.class))
                .collect(Collectors.toList());
    }
}
