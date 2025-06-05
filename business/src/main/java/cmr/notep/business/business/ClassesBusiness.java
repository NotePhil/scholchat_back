package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
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

    public Classes creerClasse(Classes classes, String professeurId) throws SchoolException {
        ClassesEntity classesEntity = dozerMapperBean.map(classes, ClassesEntity.class);
        if (classesEntity.getCodeActivation() == null) {
            classesEntity.setCodeActivation(generateActivationCode());
        }

        // Set the professor as moderator
        ProfesseursEntity professeur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(professeurId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur non trouvé"));
        classesEntity.setModerator(professeur);

        ClassesEntity savedEntity = daoAccessorService.getRepository(ClassesRepository.class)
                .save(classesEntity);
        log.info("Classe créée avec succès: {}", savedEntity.getId());
        return dozerMapperBean.map(savedEntity, Classes.class);
    }

    public Classes modifierClasse(String idClasse, Classes classeModifiee, String professeurId) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classeExistante = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));

        // Check if the professor is the moderator of this class
        if (classeExistante.getModerator() == null || !professeurId.equals(classeExistante.getModerator().getId())) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, "Seul le modérateur de la classe peut la modifier");
        }

        // Basic field updates
        classeExistante.setNom(classeModifiee.getNom());
        classeExistante.setNiveau(classeModifiee.getNiveau());
        classeExistante.setEtat(classeModifiee.getEtat());
        classeExistante.setCodeActivation(classeModifiee.getCodeActivation());

        // Etablissement Update
        if (classeModifiee.getEtablissement() != null) {
            EtablissementEntity etablissement = daoAccessorService
                    .getRepository(EtablissementRepository.class)
                    .findById(classeModifiee.getEtablissement().getId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement introuvable"));
            classeExistante.setEtablissement(etablissement);
        }

        ClassesEntity classeSauvegardee = classesRepository.save(classeExistante);
        log.info("Classe modifiée avec succès: {}", idClasse);
        return dozerMapperBean.map(classeSauvegardee, Classes.class);
    }

    public void supprimerClasse(String idClasse, String professeurId) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));

        // Check if the professor is the moderator of this class
        if (classe.getModerator() == null || !professeurId.equals(classe.getModerator().getId())) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, "Seul le modérateur de la classe peut la supprimer");
        }

        classesRepository.deleteById(idClasse);
        log.info("Classe supprimée avec succès: {}", idClasse);
    }

    public Classes obtenirClasseParId(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classeEntity = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));
        return dozerMapperBean.map(classeEntity, Classes.class);
    }

    public List<Classes> obtenirToutesLesClasses() throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        return classesRepository.findAll()
                .stream()
                .map(c -> dozerMapperBean.map(c, Classes.class))
                .collect(Collectors.toList());
    }

    // Student management methods
    public Classes ajouterEleve(String idClasse, String idEleve, String professeurId) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        // Check if the professor is the moderator of this class
        if (classe.getModerator() == null || !professeurId.equals(classe.getModerator().getId())) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, "Seul le modérateur de la classe peut ajouter des élèves");
        }

        ElevesEntity eleve = daoAccessorService.getRepository(ElevesRepository.class)
                .findById(idEleve)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Élève non trouvé"));

        if (!classe.getElevesEntities().contains(eleve)) {
            classe.getElevesEntities().add(eleve);
            classesRepository.save(classe);
        }

        return dozerMapperBean.map(classe, Classes.class);
    }

    public Classes supprimerEleve(String idClasse, String idEleve, String professeurId) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        // Check if the professor is the moderator of this class
        if (classe.getModerator() == null || !professeurId.equals(classe.getModerator().getId())) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, "Seul le modérateur de la classe peut supprimer des élèves");
        }

        ElevesEntity eleve = daoAccessorService.getRepository(ElevesRepository.class)
                .findById(idEleve)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Élève non trouvé"));

        classe.getElevesEntities().remove(eleve);
        classesRepository.save(classe);

        return dozerMapperBean.map(classe, Classes.class);
    }

    // Parent management methods
    public Classes ajouterParent(String idClasse, String idParent, String professeurId) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        // Check if the professor is the moderator of this class
        if (classe.getModerator() == null || !professeurId.equals(classe.getModerator().getId())) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, "Seul le modérateur de la classe peut ajouter des parents");
        }

        ParentsEntity parent = daoAccessorService.getRepository(ParentsRepository.class)
                .findById(idParent)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent non trouvé"));

        if (!classe.getParentsEntities().contains(parent)) {
            classe.getParentsEntities().add(parent);
            classesRepository.save(classe);
        }

        return dozerMapperBean.map(classe, Classes.class);
    }

    public Classes supprimerParent(String idClasse, String idParent, String professeurId) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        // Check if the professor is the moderator of this class
        if (classe.getModerator() == null || !professeurId.equals(classe.getModerator().getId())) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, "Seul le modérateur de la classe peut supprimer des parents");
        }

        ParentsEntity parent = daoAccessorService.getRepository(ParentsRepository.class)
                .findById(idParent)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent non trouvé"));

        classe.getParentsEntities().remove(parent);
        classesRepository.save(classe);

        return dozerMapperBean.map(classe, Classes.class);
    }

    private String generateActivationCode() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }
}