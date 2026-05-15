package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.dto.ClasseAvecDroitDto;
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
        UtilisateursEntity entity = droit.getUtilisateur();
        Utilisateurs user = mapToSpecificUserType(entity);
        // Clear sensitive data
        user.setPasseAccess(null);
        user.setActivationToken(null);
        user.setResetPasswordToken(null);
        return user;
    }

    private Utilisateurs mapToSpecificUserType(UtilisateursEntity entity) {
        if (entity instanceof ProfesseursEntity) {
            return dozerMapperBean.map(entity, cmr.notep.interfaces.modeles.Professeurs.class);
        } else if (entity instanceof ElevesEntity) {
            return dozerMapperBean.map(entity, cmr.notep.interfaces.modeles.Eleves.class);
        } else if (entity instanceof ParentsEntity) {
            return dozerMapperBean.map(entity, cmr.notep.interfaces.modeles.Parents.class);
        } else if (entity instanceof RepetiteursEntity) {
            return dozerMapperBean.map(entity, cmr.notep.interfaces.modeles.Repetiteurs.class);
        } else if (entity instanceof GestionnairesEntity) {
            return dozerMapperBean.map(entity, cmr.notep.interfaces.modeles.Gestionnaires.class);
        }
        return dozerMapperBean.map(entity, Utilisateurs.class);
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
        mapped.setEtablissement(null);
        // Keep minimal moderator info (id + name) for frontend display
        if (classe.getModerator() != null && mapped.getModerator() != null) {
            mapped.getModerator().setPasseAccess(null);
            mapped.getModerator().setActivationToken(null);
            mapped.getModerator().setResetPasswordToken(null);
        }
        return mapped;
    }

    /**
     * Returns classes where the user has publication rights,
     * each wrapped with peutPublier and peutModerer flags.
     * This lets the frontend distinguish:
     *   - peutModerer=true  → user created/moderates the class
     *   - peutModerer=false → rights were granted by someone else
     */
    public List<ClasseAvecDroitDto> obtenirClassesAvecDroitsDetail(String utilisateurId) throws SchoolException {
        log.info("Getting classes with rights detail for user {}", utilisateurId);

        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(utilisateurId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "User not found"));

        List<ClasseAvecDroitDto> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // 1. For professors: moderated classes (user is the moderator of the class)
        if (utilisateur instanceof ProfesseursEntity) {
            ProfesseursEntity prof = (ProfesseursEntity) utilisateur;
            if (prof.getModeratedClasses() != null) {
                for (ClassesEntity classe : prof.getModeratedClasses()) {
                    if (seen.add(classe.getId())) {
                        Classes mapped = mapClassWithMinimalData(classe);
                        // estCreateur=true only if this user originally created the class
                        boolean estCreateur = utilisateurId.equals(classe.getCreatorId());
                        result.add(new ClasseAvecDroitDto(mapped, true, true, estCreateur));
                    }
                }
            }
        }

        // 2. Classes from droits_publication table where user is NOT the moderator → granted rights
        List<DroitPublicationEntity> droits = daoAccessorService
                .getRepository(DroitPublicationRepository.class)
                .findAllClassesByUserId(utilisateurId);

        for (DroitPublicationEntity droit : droits) {
            ClassesEntity classe = droit.getClasse();
            if (classe != null && seen.add(classe.getId())) {
                // Only add here if user is NOT the moderator of this class
                boolean isModerator = classe.getModerator() != null
                        && utilisateurId.equals(classe.getModerator().getId());
                if (!isModerator) {
                    Classes mapped = mapClassWithMinimalData(classe);
                    result.add(new ClasseAvecDroitDto(mapped, droit.isPeutPublier(), false));
                }
            }
        }

        return result;
    }
}