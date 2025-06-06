package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
import cmr.notep.ressourcesjpa.dao.ParentsEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.ElevesRepository;
import cmr.notep.ressourcesjpa.repository.EtablissementRepository;
import cmr.notep.ressourcesjpa.repository.ParentsRepository;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cmr.notep.modele.DroitPublication;
import cmr.notep.ressourcesjpa.dao.HistoActivationEntity;
import cmr.notep.ressourcesjpa.repository.HistoActivationRepository;
import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ClassesBusiness {

    private final DaoAccessorService daoAccessorService;

    public ClassesBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Classes creerClasse(Classes classes) throws SchoolException {
        ClassesEntity classesEntity = dozerMapperBean.map(classes, ClassesEntity.class);

        // Generate a random activation code if not provided
        if (classesEntity.getCodeActivation() == null) {
            classesEntity.setCodeActivation(generateActivationCode());
        }

        // Handle moderator if provided
        if (classes.getModerator() != null && classes.getModerator().getId() != null) {
            ProfesseursEntity moderator = daoAccessorService
                    .getRepository(ProfesseursRepository.class)
                    .findById(classes.getModerator().getId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Modérateur introuvable"));
            classesEntity.setModerator(moderator);
        }

        ClassesEntity savedEntity = daoAccessorService.getRepository(ClassesRepository.class)
                .save(classesEntity);
        log.info("Classe créée avec succès: {}", savedEntity.getId());
        return dozerMapperBean.map(savedEntity, Classes.class);
    }

    public Classes modifierClasse(String idClasse, Classes classeModifiee) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);

        // First, check if the class exists
        ClassesEntity classeExistante = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));

        // Basic field updates
        classeExistante.setNom(classeModifiee.getNom());
        classeExistante.setNiveau(classeModifiee.getNiveau());
        classeExistante.setEtat(classeModifiee.getEtat());
        classeExistante.setDateCreation(classeModifiee.getDateCreation());
        classeExistante.setCodeActivation(classeModifiee.getCodeActivation());

        // Etablissement Update
        if (classeModifiee.getEtablissement() != null) {
            EtablissementEntity etablissement = daoAccessorService
                    .getRepository(EtablissementRepository.class)
                    .findById(classeModifiee.getEtablissement().getId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement introuvable"));
            classeExistante.setEtablissement(etablissement);
        }

        // Moderator Update
        if (classeModifiee.getModerator() != null && classeModifiee.getModerator().getId() != null) {
            ProfesseursEntity moderator = daoAccessorService
                    .getRepository(ProfesseursRepository.class)
                    .findById(classeModifiee.getModerator().getId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Modérateur introuvable"));
            classeExistante.setModerator(moderator);
        }

        // Parents Update
        if (classeModifiee.getParents() != null) {
            classeExistante.getParentsEntities().clear();
            for (Parents parent : classeModifiee.getParents()) {
                ParentsEntity parentEntity = daoAccessorService
                        .getRepository(ParentsRepository.class)
                        .findById(parent.getId())
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable"));
                classeExistante.getParentsEntities().add(parentEntity);
            }
        }

        // Eleves Update
        if (classeModifiee.getEleves() != null) {
            classeExistante.getElevesEntities().clear();
            for (Eleves eleve : classeModifiee.getEleves()) {
                ElevesEntity eleveEntity = daoAccessorService
                        .getRepository(ElevesRepository.class)
                        .findById(eleve.getId())
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "élève introuvable"));
                classeExistante.getElevesEntities().add(eleveEntity);
            }
        }

        // Sauvegarde de la classe mise à jour
        ClassesEntity classeSauvegardee = classesRepository.save(classeExistante);
        log.info("Classe modifiée avec succès: {}", idClasse);
        return dozerMapperBean.map(classeSauvegardee, Classes.class);
    }

    public void supprimerClasse(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        // Vérifier si la classe existe avant de supprimer
        if (!classesRepository.existsById(idClasse)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse);
        }
        classesRepository.deleteById(idClasse);
        log.info("Classe supprimée avec succès: {}", idClasse);
    }

    public Classes obtenirClasseParId(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classeEntity = classesRepository.findById(idClasse)
                .orElseThrow(() -> {
                    return new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse);
                });
        return dozerMapperBean.map(classeEntity, Classes.class);
    }

    public List<Classes> obtenirToutesLesClasses() throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        return classesRepository.findAll()
                .stream()
                .map(c -> dozerMapperBean.map(c, Classes.class))
                .collect(Collectors.toList());
    }
    public Classes modifierDroitPublication(String idClasse, DroitPublication droitPublication) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classeExistante = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));

        classeExistante.setDroitPublication(droitPublication);
        ClassesEntity updatedEntity = classesRepository.save(classeExistante);
        log.info("Droit de publication modifié pour la classe: {}", idClasse);
        return dozerMapperBean.map(updatedEntity, Classes.class);
    }

    // Method to generate a random activation code
    private String generateActivationCode() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }
}