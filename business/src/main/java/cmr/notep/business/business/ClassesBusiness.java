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
import cmr.notep.modele.EtatClasse;
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

    /**
     * Creates a new class with default status EN_ATTENTE_APPROBATION
     */
    public Classes creerClasse(Classes classes) throws SchoolException {
        ClassesEntity classesEntity = dozerMapperBean.map(classes, ClassesEntity.class);

        // Set default status if not provided
        if (classesEntity.getEtat() == null) {
            classesEntity.setEtat(EtatClasse.EN_ATTENTE_APPROBATION);
        }

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
            moderator.getModeratedClasses().add(classesEntity);
            daoAccessorService.getRepository(ProfesseursRepository.class).save(moderator);
        }

        ClassesEntity savedEntity = daoAccessorService.getRepository(ClassesRepository.class)
                .save(classesEntity);
        log.info("Classe créée avec succès: {}", savedEntity.getId());
        return dozerMapperBean.map(savedEntity, Classes.class);
    }

    /**
     * Updates class information including status
     */
    public Classes modifierClasse(String idClasse, Classes classeModifiee) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);

        ClassesEntity classeExistante = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));
// Handle moderator update
        if (classeModifiee.getModerator() != null && classeModifiee.getModerator().getId() != null) {
            ProfesseursEntity oldModerator = classeExistante.getModerator();
            if (oldModerator != null) {
                oldModerator.getModeratedClasses().remove(classeExistante);
                daoAccessorService.getRepository(ProfesseursRepository.class).save(oldModerator);
            }

            ProfesseursEntity newModerator = daoAccessorService
                    .getRepository(ProfesseursRepository.class)
                    .findById(classeModifiee.getModerator().getId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Modérateur introuvable"));

            classeExistante.setModerator(newModerator);
            newModerator.getModeratedClasses().add(classeExistante);
            daoAccessorService.getRepository(ProfesseursRepository.class).save(newModerator);
        } else if (classeExistante.getModerator() != null) {
            // Remove moderator if none is specified in the update
            ProfesseursEntity oldModerator = classeExistante.getModerator();
            oldModerator.getModeratedClasses().remove(classeExistante);
            daoAccessorService.getRepository(ProfesseursRepository.class).save(oldModerator);
            classeExistante.setModerator(null);
        }
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

        ClassesEntity classeSauvegardee = classesRepository.save(classeExistante);
        log.info("Classe modifiée avec succès: {}", idClasse);
        return dozerMapperBean.map(classeSauvegardee, Classes.class);
    }

    /**
     * Approves a pending class (changes status from EN_ATTENTE_APPROBATION to ACTIF)
     */
    public Classes approuverClasse(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        if (classe.getEtat() != EtatClasse.EN_ATTENTE_APPROBATION) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                    "Seules les classes en attente peuvent être approuvées");
        }

        classe.setEtat(EtatClasse.ACTIF);
        ClassesEntity saved = classesRepository.save(classe);
        return dozerMapperBean.map(saved, Classes.class);
    }

    /**
     * Rejects a pending class (changes status from EN_ATTENTE_APPROBATION to INACTIF)
     */
    public Classes rejeterClasse(String idClasse, String motif) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        if (classe.getEtat() != EtatClasse.EN_ATTENTE_APPROBATION) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                    "Seules les classes en attente peuvent être rejetées");
        }

        classe.setEtat(EtatClasse.INACTIF);
        // You might want to store the rejection reason in a separate table
        ClassesEntity saved = classesRepository.save(classe);
        return dozerMapperBean.map(saved, Classes.class);
    }

    public void supprimerClasse(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        if (!classesRepository.existsById(idClasse)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse);
        }
        classesRepository.deleteById(idClasse);
        log.info("Classe supprimée avec succès: {}", idClasse);
    }

    public Classes obtenirClasseParId(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classeEntity = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));

        Classes classe = dozerMapperBean.map(classeEntity, Classes.class);

        // Map moderated classes to just IDs to prevent circular references
        if (classeEntity.getModerator() != null) {
            Professeurs moderator = new Professeurs();
            moderator.setId(classeEntity.getModerator().getId());
            moderator.setNom(classeEntity.getModerator().getNom());
            moderator.setPrenom(classeEntity.getModerator().getPrenom());
            classe.setModerator(moderator);
        }

        return classe;
    }

    /**
     * Gets all classes with a specific status
     */
    public List<Classes> obtenirClassesParEtat(EtatClasse etat) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        return classesRepository.findByEtat(etat)
                .stream()
                .map(c -> dozerMapperBean.map(c, Classes.class))
                .collect(Collectors.toList());
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

    private String generateActivationCode() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }
}