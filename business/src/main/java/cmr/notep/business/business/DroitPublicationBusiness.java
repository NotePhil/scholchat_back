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

import java.util.*;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class DroitPublicationBusiness {

    private final DaoAccessorService daoAccessorService;

    public DroitPublicationBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public void attribuerDroitPublication(String utilisateurId, String classeId, boolean peutPublier, boolean peutModerer)
            throws SchoolException {

        log.info("Assigning publication rights to user {} for class {}", utilisateurId, classeId);

        // Verify user exists and is a professor
        UtilisateursEntity utilisateur = verifyUserIsProfessor(utilisateurId);

        // Verify class exists (any status)
        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                .findById(classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Class not found"));

        // Check if rights already exist
        if (daoAccessorService.getRepository(DroitPublicationRepository.class)
                .existsByUtilisateurIdAndClasseId(utilisateurId, classeId)) {
            throw new SchoolException(SchoolErrorCode.ALREADY_EXISTS,
                    "User already has publication rights for this class");
        }

        createPublicationRight(utilisateurId, classeId, utilisateur, classe, peutPublier, peutModerer);
    }

    private UtilisateursEntity verifyUserIsProfessor(String userId) throws SchoolException {
        UtilisateursEntity user = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(userId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "User not found"));

        if (!(user instanceof ProfesseursEntity)) {
            throw new SchoolException(SchoolErrorCode.INVALID_OPERATION,
                    "Only professors can have publication rights");
        }
        return user;
    }

    private void createPublicationRight(String userId, String classId,
                                        UtilisateursEntity user, ClassesEntity classe,
                                        boolean canPublish, boolean canModerate) {
        DroitPublicationEntity droit = new DroitPublicationEntity();
        droit.setUtilisateurId(userId);
        droit.setClasseId(classId);
        droit.setUtilisateur(user);
        droit.setClasse(classe);
        droit.setDateAttribution(new Date());
        droit.setPeutPublier(canPublish);
        droit.setPeutModerer(canModerate);

        daoAccessorService.getRepository(DroitPublicationRepository.class).save(droit);
        log.info("Publication rights successfully assigned");
    }

    public void modifierDroitPublication(String utilisateurId, String classeId,
                                         boolean peutPublier, boolean peutModerer)
            throws SchoolException {

        log.info("Updating publication rights for user {} in class {}", utilisateurId, classeId);

        DroitPublicationEntity droit = daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findByUtilisateurIdAndClasseId(utilisateurId, classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Publication rights not found"));

        droit.setPeutPublier(peutPublier);
        droit.setPeutModerer(peutModerer);

        daoAccessorService.getRepository(DroitPublicationRepository.class).save(droit);
        log.info("Publication rights successfully updated");
    }

    public void retirerDroitPublication(String utilisateurId, String classeId) throws SchoolException {
        log.info("Removing publication rights from user {} for class {}", utilisateurId, classeId);

        DroitPublicationEntity droit = daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findByUtilisateurIdAndClasseId(utilisateurId, classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Publication rights not found"));

        daoAccessorService.getRepository(DroitPublicationRepository.class).delete(droit);
        log.info("Publication rights successfully removed");
    }

    public List<Utilisateurs> obtenirUtilisateursAvecDroitPublication(String classeId) throws SchoolException {
        log.info("Getting users with publication rights for class {}", classeId);

        // Verify class exists (any status)
        if (!daoAccessorService.getRepository(ClassesRepository.class).existsById(classeId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Class not found");
        }

        return daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findAllUsersByClassId(classeId)
                .stream()
                .map(this::mapUserWithMinimalData)
                .collect(Collectors.toList());
    }

    private Utilisateurs mapUserWithMinimalData(DroitPublicationEntity droit) {
        Utilisateurs user = dozerMapperBean.map(droit.getUtilisateur(), Utilisateurs.class);
        // Clear sensitive data
        user.setPasseAccess(null);
        user.setActivationToken(null);
        user.setResetPasswordToken(null);
        return user;
    }

    public List<Classes> obtenirClassesAvecDroitPublication(String utilisateurId) throws SchoolException {
        log.info("Getting classes with publication rights for user {}", utilisateurId);

        // Verify user exists
        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(utilisateurId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "User not found"));

        if (Boolean.TRUE.equals(utilisateur.getAdmin())) {
            return getAllClassesForAdmin();
        }

        // For professors, include moderated classes + classes with publication rights
        if (utilisateur instanceof ProfesseursEntity) {
            return getClassesForProfessor((ProfesseursEntity) utilisateur, utilisateurId);
        }

        return getClassesWithPublicationRights(utilisateurId);
    }

    private List<Classes> getAllClassesForAdmin() {
        return daoAccessorService.getRepository(ClassesRepository.class)
                .findAll()
                .stream()
                .map(this::mapClassWithMinimalData)
                .collect(Collectors.toList());
    }

    private List<Classes> getClassesForProfessor(ProfesseursEntity professor, String userId) {
        Set<String> classIds = new HashSet<>();
        List<Classes> result = new ArrayList<>();

        // Add moderated classes
        if (professor.getModeratedClasses() != null) {
            for (ClassesEntity classe : professor.getModeratedClasses()) {
                if (classIds.add(classe.getId())) {
                    result.add(mapClassWithMinimalData(classe));
                }
            }
        }

        // Add classes with publication rights
        List<DroitPublicationEntity> droits = daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findAllClassesByUserId(userId);
        for (DroitPublicationEntity droit : droits) {
            if (droit.getClasse() != null && classIds.add(droit.getClasse().getId())) {
                result.add(mapClassWithMinimalData(droit.getClasse()));
            }
        }

        return result;
    }

    private List<Classes> getClassesWithPublicationRights(String userId) {
        return daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findAllClassesByUserId(userId)
                .stream()
                .map(DroitPublicationEntity::getClasse)
                .filter(Objects::nonNull)
                .map(this::mapClassWithMinimalData)
                .collect(Collectors.toList());
    }

    private Classes mapClassWithMinimalData(ClassesEntity classe) {
        Classes mapped = dozerMapperBean.map(classe, Classes.class);
        // Simplify response by removing nested objects
        mapped.setEtablissement(null);
        mapped.setModerator(null);
        return mapped;
    }
}