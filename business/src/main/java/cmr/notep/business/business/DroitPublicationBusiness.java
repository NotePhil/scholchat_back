package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class DroitPublicationBusiness {

    private final DaoAccessorService daoAccessorService;

    public DroitPublicationBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public void attribuerDroitPublication(String utilisateurId, String classeId, boolean peutPublier, boolean peutModerer) throws SchoolException {
        log.info("Attribuer droit de publication à l'utilisateur {} pour la classe {}", utilisateurId, classeId);

        // Vérifier si l'utilisateur est un professeur
        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(utilisateurId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable"));

        if (!(utilisateur instanceof ProfesseursEntity)) {
            throw new SchoolException(SchoolErrorCode.INVALID_OPERATION,
                    "Seuls les professeurs peuvent avoir des droits de publication");
        }

        // Vérifier si le droit existe déjà
        Optional<DroitPublicationEntity> droitExist = daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findByUtilisateurIdAndClasseId(utilisateurId, classeId);

        if (droitExist.isPresent()) {
            throw new SchoolException(SchoolErrorCode.ALREADY_EXISTS,
                    "L'utilisateur a déjà des droits de publication pour cette classe");
        }

        // Vérifier l'existence de la classe
        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                .findById(classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable"));

        // Créer le nouveau droit
        DroitPublicationEntity droit = new DroitPublicationEntity();
        droit.setUtilisateurId(utilisateurId);
        droit.setClasseId(classeId);
        droit.setUtilisateur(utilisateur);
        droit.setClasse(classe);
        droit.setDateAttribution(new Date());
        droit.setPeutPublier(peutPublier);
        droit.setPeutModerer(peutModerer);

        // Sauvegarder
        daoAccessorService.getRepository(DroitPublicationRepository.class).save(droit);
        log.info("Droit de publication attribué avec succès");
    }

    public void modifierDroitPublication(String utilisateurId, String classeId, boolean peutPublier, boolean peutModerer) throws SchoolException {
        log.info("Modifier droit de publication pour l'utilisateur {} dans la classe {}", utilisateurId, classeId);

        DroitPublicationEntity droit = daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findByUtilisateurIdAndClasseId(utilisateurId, classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Droit de publication introuvable"));

        droit.setPeutPublier(peutPublier);
        droit.setPeutModerer(peutModerer);

        daoAccessorService.getRepository(DroitPublicationRepository.class).save(droit);
        log.info("Droit de publication modifié avec succès");
    }

    public void retirerDroitPublication(String utilisateurId, String classeId) throws SchoolException {
        log.info("Retirer droit de publication de l'utilisateur {} pour la classe {}", utilisateurId, classeId);

        DroitPublicationEntity droit = daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findByUtilisateurIdAndClasseId(utilisateurId, classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Droit de publication introuvable"));

        daoAccessorService.getRepository(DroitPublicationRepository.class).delete(droit);
        log.info("Droit de publication retiré avec succès");
    }

    public List<Utilisateurs> obtenirUtilisateursAvecDroitPublication(String classeId) throws SchoolException {
        log.info("Obtenir tous les utilisateurs ayant des droits de publication pour la classe {}", classeId);

        // Vérifier l'existence de la classe
        if (!daoAccessorService.getRepository(ClassesRepository.class).existsById(classeId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable");
        }

        return daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findByClasseId(classeId)
                .stream()
                .map(droit -> dozerMapperBean.map(droit.getUtilisateur(), Utilisateurs.class))
                .collect(Collectors.toList());
    }

    public List<Classes> obtenirClassesAvecDroitPublication(String utilisateurId) throws SchoolException {
        log.info("Obtenir toutes les classes où l'utilisateur {} a des droits de publication", utilisateurId);

        // Vérifier l'existence de l'utilisateur
        if (!daoAccessorService.getRepository(UtilisateursRepository.class).existsById(utilisateurId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable");
        }

        return daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findByUtilisateurId(utilisateurId)
                .stream()
                .map(droit -> dozerMapperBean.map(droit.getClasse(), Classes.class))
                .collect(Collectors.toList());
    }
}