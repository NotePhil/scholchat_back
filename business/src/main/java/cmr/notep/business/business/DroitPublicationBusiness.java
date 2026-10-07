package cmr.notep.business.business;

import cmr.notep.business.security.UserSubtypeService;

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
    private final UserSubtypeService userSubtypeService;

    public DroitPublicationBusiness(DaoAccessorService daoAccessorService, UserSubtypeService userSubtypeService) {
        this.daoAccessorService = daoAccessorService;
        this.userSubtypeService = userSubtypeService;
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

        // Rôle lu dans la table professeurs : le sous-type chargé est arbitraire pour un compte multi-rôles
        if (!userSubtypeService.isProfesseur(userId)) {
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
        String type = userSubtypeService.typeUtilisateur(entity.getId());
        if ("PROFESSEUR".equals(type)) {
            return dozerMapperBean.map(entity, cmr.notep.interfaces.modeles.Professeurs.class);
        } else if ("ELEVE".equals(type)) {
            return dozerMapperBean.map(entity, cmr.notep.interfaces.modeles.Eleves.class);
        } else if ("PARENT".equals(type)) {
            return dozerMapperBean.map(entity, cmr.notep.interfaces.modeles.Parents.class);
        } else if ("REPETITEUR".equals(type)) {
            return dozerMapperBean.map(entity, cmr.notep.interfaces.modeles.Repetiteurs.class);
        } else if ("GESTIONNAIRE".equals(type)) {
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
        if (userSubtypeService.isProfesseur(utilisateurId)) {
            return getClassesForProfessor(utilisateurId);
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

    private List<Classes> getClassesForProfessor(String userId) {
        Set<String> classIds = new HashSet<>();
        List<Classes> result = new ArrayList<>();

        // Add moderated classes
        for (ClassesEntity classe : daoAccessorService.getRepository(ClassesRepository.class).findByModeratorId(userId)) {
            if (classIds.add(classe.getId())) {
                result.add(mapClassWithMinimalData(classe));
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
     * "Mes classes" d'un professeur : union, dédupliquée avec le rôle le plus fort, de
     *   - CREATEUR    : classes créées par l'utilisateur (creatorId), même modérées par un autre ;
     *   - MODERATEUR  : classes dont il est modérateur principal ou co-modérateur
     *                   (professeur_classes_moderees, cf. AccessControlService.isClassManager) ;
     *   - PUBLICATION : classes où un droit de publication lui a été accordé (peutModerer = droit délégué).
     * Chaque entrée porte le nom du créateur (creatorNom) et du modérateur principal (moderateurNom).
     */
    public List<ClasseAvecDroitDto> obtenirClassesAvecDroitsDetail(String utilisateurId) throws SchoolException {
        log.info("Getting classes with rights detail for user {}", utilisateurId);

        daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(utilisateurId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "User not found"));

        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        // Ordre d'insertion conservé : créées/modérées d'abord, puis droits accordés
        Map<String, ClassesEntity> classes = new LinkedHashMap<>();
        Set<String> moderees = new HashSet<>();
        Map<String, DroitPublicationEntity> droitsParClasse = new HashMap<>();

        for (ClassesEntity c : classesRepository.findByCreatorId(utilisateurId)) {
            classes.putIfAbsent(c.getId(), c);
        }
        for (ClassesEntity c : classesRepository.findByModeratorId(utilisateurId)) {
            classes.putIfAbsent(c.getId(), c);
            moderees.add(c.getId());
        }
        List<String> coModerees;
        try {
            coModerees = daoAccessorService.getRepository(ProfesseursRepository.class).findClassIdsModeratedBy(utilisateurId);
        } catch (Exception e) {
            // table de co-modération absente : ignorée (même tolérance que AccessControlService)
            coModerees = Collections.emptyList();
        }
        for (String classeId : coModerees) {
            if (!classes.containsKey(classeId)) {
                classesRepository.findById(classeId).ifPresent(c -> classes.put(c.getId(), c));
            }
            if (classes.containsKey(classeId)) moderees.add(classeId);
        }
        for (DroitPublicationEntity droit : daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findAllClassesByUserId(utilisateurId)) {
            ClassesEntity c = droit.getClasse();
            if (c == null) continue;
            classes.putIfAbsent(c.getId(), c);
            droitsParClasse.put(c.getId(), droit);
        }

        Map<String, String> nomsCache = new HashMap<>();
        List<ClasseAvecDroitDto> result = new ArrayList<>();
        for (ClassesEntity classe : classes.values()) {
            boolean estCreateur = utilisateurId.equals(classe.getCreatorId());
            boolean estModerateur = moderees.contains(classe.getId());
            DroitPublicationEntity droit = droitsParClasse.get(classe.getId());

            ClasseAvecDroitDto dto;
            if (estCreateur || estModerateur) {
                // Gestionnaire de la classe (isClassManager) : publie et modère
                dto = new ClasseAvecDroitDto(mapClassWithMinimalData(classe), true, true, estCreateur);
                dto.setRole(estCreateur ? ClasseAvecDroitDto.ROLE_CREATEUR : ClasseAvecDroitDto.ROLE_MODERATEUR);
            } else {
                dto = new ClasseAvecDroitDto(mapClassWithMinimalData(classe),
                        droit != null && droit.isPeutPublier(), droit != null && droit.isPeutModerer(), false);
                dto.setRole(ClasseAvecDroitDto.ROLE_PUBLICATION);
            }
            dto.setModerateurNom(classe.getModerator() != null ? nomComplet(classe.getModerator()) : null);
            dto.setCreatorNom(nomUtilisateur(classe.getCreatorId(), nomsCache));
            result.add(dto);
        }
        return result;
    }

    private String nomUtilisateur(String userId, Map<String, String> cache) {
        if (userId == null || userId.isBlank()) return null;
        return cache.computeIfAbsent(userId, id -> daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(id).map(this::nomComplet).orElse(null));
    }

    private String nomComplet(UtilisateursEntity u) {
        String nom = ((u.getPrenom() != null ? u.getPrenom() : "") + " " + (u.getNom() != null ? u.getNom() : "")).trim();
        return nom.isEmpty() ? null : nom;
    }
}
