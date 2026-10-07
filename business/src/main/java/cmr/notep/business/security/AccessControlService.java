package cmr.notep.business.security;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static cmr.notep.business.security.CurrentUserService.forbidden;

/**
 * Règles d'autorisation métier réutilisables (propriété / appartenance).
 *
 * Toutes les méthodes {@code requireXxx} laissent passer un administrateur, lèvent
 * UNAUTHORIZED (401) pour un appel anonyme et OPERATION_INTERDITE (403) sinon.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessControlService {

    private final CurrentUserService currentUser;
    private final ClassesRepository classesRepository;
    private final ProfesseursRepository professeursRepository;
    private final AccederRepository accederRepository;
    private final DroitPublicationRepository droitPublicationRepository;
    private final ParentEleveRepository parentEleveRepository;
    private final EtablissementRepository etablissementRepository;
    private final CoursRepository coursRepository;
    private final CoursProgrammerRepository coursProgrammerRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseProgrammerRepository exerciseProgrammerRepository;
    private final QuestionReponseRepository questionReponseRepository;
    private final UtilisateursRepository utilisateursRepository;
    private final DemandeAccesRepository demandeAccesRepository;
    private final CanalRepository canalRepository;
    private final ParticiperExoRepository participerExoRepository;
    private final UserRoleRepository userRoleRepository;
    private final cmr.notep.business.utils.JwtUtil jwtUtil;

    // ─── Utilisateurs / parents ───────────────────────────────────────────────

    public boolean isParentOf(String parentId, String eleveId) {
        return parentId != null && eleveId != null
                && parentEleveRepository.existsByParentIdAndEleveId(parentId, eleveId);
    }

    /**
     * Le parent connecté agit pour un enfant mineur, c.-à-d. un enfant rattaché qui n'a pas de compte
     * personnel (aucun mot de passe : il ne peut pas se connecter). C'est le seul moyen pour un mineur
     * de rendre un devoir. Un enfant ayant son propre compte (majeur) répond lui-même.
     */
    public boolean canActForMinorChild(String eleveId) {
        String me = currentUser.currentUserIdOpt().orElse(null);
        if (me == null || !isParentOf(me, eleveId)) return false;
        return utilisateursRepository.findById(eleveId)
                .map(u -> u.getPasseAccess() == null || u.getPasseAccess().isBlank())
                .orElse(false);
    }

    /** L'utilisateur lui-même, un de ses parents, ou un administrateur. */
    public void requireSelfOrParentOrAdmin(String userId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin() || me.equals(userId) || isParentOf(me, userId)) {
            return;
        }
        throw forbidden("Vous ne pouvez accéder qu'à vos propres données (ou à celles de vos enfants).");
    }

    /** Professeur dont le profil (pièces) a été validé par l'administrateur : seul cas qui donne des droits. */
    public boolean isProfessor(String userId) {
        return userId != null && utilisateursRepository.isProfesseurValide(userId);
    }

    // ─── Classes ──────────────────────────────────────────────────────────────

    private static String required(String id, String what) {
        if (id == null || id.isBlank()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Identifiant requis : " + what);
        }
        return id;
    }

    public ClassesEntity getClasse(String classeId) {
        return classesRepository.findById(required(classeId, "classe"))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable"));
    }

    public boolean isEtablissementGestionnaire(EtablissementEntity etablissement, String userId) {
        return etablissement != null && etablissement.getGestionnaire() != null
                && Objects.equals(etablissement.getGestionnaire().getId(), userId);
    }

    public boolean isEtablissementGestionnaire(String etablissementId, String userId) {
        if (etablissementId == null || userId == null) return false;
        return etablissementRepository.findById(etablissementId)
                .map(e -> isEtablissementGestionnaire(e, userId))
                .orElse(false);
    }

    /**
     * Gestionnaire de la classe : modérateur principal, créateur, co-modérateur
     * (professeur_classes_moderees ou droit "peutModerer"), ou gestionnaire de
     * l'établissement de rattachement.
     */
    public boolean isClassManager(String classeId, String userId) {
        if (classeId == null || userId == null) return false;
        ClassesEntity classe = classesRepository.findById(classeId).orElse(null);
        if (classe == null) return false;
        if (classe.getModerator() != null && userId.equals(classe.getModerator().getId())) return true;
        if (userId.equals(classe.getCreatorId())) return true;
        if (isEtablissementGestionnaire(classe.getEtablissement(), userId)) return true;
        try {
            if (professeursRepository.findModeratorIdsForClass(classeId).contains(userId)) return true;
        } catch (Exception ignored) {
            // table de co-modération absente : on ignore
        }
        return droitPublicationRepository.findByUtilisateurIdAndClasseId(userId, classeId)
                .map(DroitPublicationEntity::isPeutModerer)
                .orElse(false);
    }

    public void requireClassManager(String classeId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin() || isClassManager(classeId, me)) return;
        throw forbidden("Seuls le modérateur de la classe, son établissement ou un administrateur peuvent effectuer cette action.");
    }

    /** Membre de la classe : gestionnaire, élève/parent ayant accès, ou titulaire d'un droit de publication. */
    public boolean isClassMember(String classeId, String userId) {
        if (classeId == null || userId == null) return false;
        if (accederRepository.existsByUtilisateurIdAndClasseId(userId, classeId)) return true;
        if (droitPublicationRepository.existsByUtilisateurIdAndClasseId(userId, classeId)) return true;
        if (isClassManager(classeId, userId)) return true;
        // Parent d'un élève membre de la classe
        for (String enfantId : parentEleveRepository.findEleveIdsByParentId(userId)) {
            if (accederRepository.existsByUtilisateurIdAndClasseId(enfantId, classeId)) return true;
        }
        return false;
    }

    public void requireClassMember(String classeId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin() || isClassMember(classeId, me)) return;
        throw forbidden("Vous n'êtes pas membre de cette classe.");
    }

    /** Peut publier dans la classe : gestionnaire ou titulaire du droit de publication. */
    public boolean canPublishInClass(String classeId, String userId) {
        if (isClassManager(classeId, userId)) return true;
        return droitPublicationRepository.findByUtilisateurIdAndClasseId(userId, classeId)
                .map(d -> d.isPeutPublier() || d.isPeutModerer())
                .orElse(false);
    }

    /** Professeur intervenant dans la classe (gestionnaire ou droit de publication). */
    public void requireClassTeacher(String classeId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin() || canPublishInClass(classeId, me)) return;
        throw forbidden("Vous n'avez pas les droits d'enseignant sur cette classe.");
    }

    /** Validation/rejet d'une classe en attente : administrateur ou gestionnaire de l'établissement de rattachement. */
    public void requireClassApprover(String classeId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin()) return;
        ClassesEntity classe = getClasse(classeId);
        if (isEtablissementGestionnaire(classe.getEtablissement(), me)) return;
        throw forbidden("Seuls l'établissement de rattachement ou un administrateur peuvent valider ou rejeter cette classe.");
    }

    /** Décision d'un établissement sur une classe : la classe doit lui appartenir et l'appelant en être le gestionnaire (ou admin). */
    public void requireClassDecisionByEtablissement(String classeId, String etablissementId) {
        currentUser.requireAuthenticated();
        ClassesEntity classe = getClasse(classeId);
        if (classe.getEtablissement() == null || !Objects.equals(etablissementId, classe.getEtablissement().getId())) {
            throw forbidden("Cette classe n'est pas rattachée à cet établissement.");
        }
        requireEtablissementGestionnaireOrAdmin(etablissementId);
    }

    /** Traitement d'une demande d'accès : gestionnaire de la classe concernée (ou admin). */
    public void requireDemandeManager(String demandeId) {
        DemandeAccesEntity demande = demandeAccesRepository.findById(required(demandeId, "demande"))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Demande non trouvée"));
        requireClassManager(demande.getClasse().getId());
    }

    /** Retrait d'un accès : la personne elle-même (ou son parent), le gestionnaire de la classe, ou un admin. */
    public void requireCanRemoveAccess(String utilisateurId, String classeId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin() || me.equals(utilisateurId) || isParentOf(me, utilisateurId)
                || isClassManager(classeId, me)) return;
        throw forbidden("Seuls le modérateur de la classe ou l'utilisateur concerné peuvent retirer cet accès.");
    }

    /** Canal : son professeur, le gestionnaire de sa classe, ou un admin. */
    public void requireCanalManager(String canalId) {
        String me = currentUser.requireUserId();
        CanalEntity canal = canalRepository.findById(required(canalId, "canal"))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Canal introuvable"));
        if (currentUser.isAdmin()) return;
        if (canal.getProfesseur() != null && me.equals(canal.getProfesseur().getId())) return;
        if (canal.getClasse() != null && isClassManager(canal.getClasse().getId(), me)) return;
        throw forbidden("Seuls le professeur du canal ou le modérateur de la classe peuvent le modifier.");
    }

    // ─── Établissements ───────────────────────────────────────────────────────

    public void requireEtablissementGestionnaireOrAdmin(String etablissementId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin() || isEtablissementGestionnaire(etablissementId, me)) return;
        throw forbidden("Seuls le gestionnaire de l'établissement ou un administrateur peuvent effectuer cette action.");
    }

    // ─── Cours ────────────────────────────────────────────────────────────────

    public CoursEntity getCours(String coursId) {
        return coursRepository.findById(required(coursId, "cours"))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Cours introuvable"));
    }

    public boolean isCoursAuthor(String coursId, String userId) {
        if (coursId == null || userId == null) return false;
        return coursRepository.findById(coursId)
                .map(c -> c.getRedacteur() != null && userId.equals(c.getRedacteur().getId()))
                .orElse(false);
    }

    public void requireCoursAuthor(String coursId) {
        String me = currentUser.requireUserId();
        getCours(coursId);
        if (currentUser.isAdmin() || isCoursAuthor(coursId, me)) return;
        throw forbidden("Seul l'auteur du cours ou un administrateur peut effectuer cette action.");
    }

    /**
     * Droit de suivre un cours (lecture, session en direct) : auteur, professeur programmateur,
     * tout professeur (co-enseignement, comme la règle de session existante), participant désigné,
     * membre d'une classe où le cours est programmé, ou parent d'un tel élève.
     */
    public boolean canAccessCours(String coursId, String userId) {
        if (coursId == null || userId == null) return false;
        if (isCoursAuthor(coursId, userId) || isProfessor(userId)) return true;
        List<CoursProgrammerEntity> programmations = coursProgrammerRepository.findByCoursId(coursId);
        for (CoursProgrammerEntity p : programmations) {
            if (p.getProfesseur() != null && userId.equals(p.getProfesseur().getId())) return true;
            if (p.getParticipants() != null && p.getParticipants().stream().anyMatch(u -> userId.equals(u.getId()))) {
                return true;
            }
            if (p.getClasses() != null) {
                for (ClassesEntity c : p.getClasses()) {
                    if (isClassMember(c.getId(), userId)) return true;
                }
            }
        }
        return false;
    }

    public void requireCoursProgrammeOwner(String coursProgrammeId) {
        String me = currentUser.requireUserId();
        CoursProgrammerEntity p = coursProgrammerRepository.findById(required(coursProgrammeId, "programmation"))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Programmation introuvable"));
        if (currentUser.isAdmin()) return;
        boolean owner = (p.getProfesseur() != null && me.equals(p.getProfesseur().getId()))
                || (p.getCours() != null && p.getCours().getRedacteur() != null
                    && me.equals(p.getCours().getRedacteur().getId()));
        if (!owner) {
            throw forbidden("Seul le professeur ayant programmé ce cours peut le modifier.");
        }
    }

    // ─── Exercices ────────────────────────────────────────────────────────────

    public boolean isExerciseAuthor(String exerciseId, String userId) {
        if (exerciseId == null || userId == null) return false;
        return exerciseRepository.findById(exerciseId)
                .map(e -> e.getRedacteur() != null && userId.equals(e.getRedacteur().getId()))
                .orElse(false);
    }

    public void requireExerciseAuthor(String exerciseId) {
        String me = currentUser.requireUserId();
        if (!exerciseRepository.existsById(required(exerciseId, "exercice"))) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable");
        }
        if (currentUser.isAdmin() || isExerciseAuthor(exerciseId, me)) return;
        throw forbidden("Seul l'auteur de l'exercice ou un administrateur peut effectuer cette action.");
    }

    /** Exercice réutilisable par un autre professeur : restriction PUBLIC, ou auteur. */
    public boolean isExercisePublicOrAuthor(String exerciseId, String userId) {
        if (exerciseId == null) return false;
        return exerciseRepository.findById(exerciseId)
                .map(e -> "PUBLIC".equalsIgnoreCase(e.getRestriction())
                        || (e.getRedacteur() != null && Objects.equals(userId, e.getRedacteur().getId())))
                .orElse(false);
    }

    public void requireQuestionAuthor(String questionId) {
        QuestionReponseEntity q = questionReponseRepository.findById(required(questionId, "question"))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Question introuvable"));
        if (q.getExercise() == null) {
            currentUser.requireAdmin();
            return;
        }
        requireExerciseAuthor(q.getExercise().getId());
    }

    public ExerciseProgrammerEntity getExerciseProgramme(String exerciseProgrammerId) {
        return exerciseProgrammerRepository.findById(required(exerciseProgrammerId, "exercice programmé"))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));
    }

    /** Professeur responsable d'un exercice programmé : programmateur, auteur, ou enseignant d'une classe de diffusion. */
    public boolean isExerciseProgrammeTeacher(ExerciseProgrammerEntity ep, String userId) {
        if (ep == null || userId == null) return false;
        if (ep.getProgrammePar() != null && userId.equals(ep.getProgrammePar().getId())) return true;
        if (ep.getExercise() != null && ep.getExercise().getRedacteur() != null
                && userId.equals(ep.getExercise().getRedacteur().getId())) return true;
        if (ep.getClassesDiffusees() != null) {
            for (ClassesEntity c : ep.getClassesDiffusees()) {
                if (canPublishInClass(c.getId(), userId)) return true;
            }
        }
        return false;
    }

    public void requireExerciseProgrammeTeacher(String exerciseProgrammerId) {
        String me = currentUser.requireUserId();
        ExerciseProgrammerEntity ep = getExerciseProgramme(exerciseProgrammerId);
        if (currentUser.isAdmin() || isExerciseProgrammeTeacher(ep, me)) return;
        throw forbidden("Seul le professeur responsable de cet exercice peut effectuer cette action.");
    }

    /** Professeur responsable d'au moins un exercice programmé à partir de cet exercice (ou son auteur). */
    public boolean isTeacherOfExercise(String exerciseId, String userId) {
        if (isExerciseAuthor(exerciseId, userId)) return true;
        for (ExerciseProgrammerEntity ep : exerciseProgrammerRepository.findByExerciseId(exerciseId)) {
            if (isExerciseProgrammeTeacher(ep, userId)) return true;
        }
        return false;
    }

    public void requireTeacherOfQuestion(String questionId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin()) return;
        QuestionReponseEntity q = questionReponseRepository.findById(required(questionId, "question"))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Question introuvable"));
        if (q.getExercise() != null && isTeacherOfExercise(q.getExercise().getId(), me)) return;
        throw forbidden("Seul le professeur responsable de cet exercice peut corriger ces réponses.");
    }

    /** Lecture des copies/notes d'un élève : lui-même, son parent, un admin ou un professeur. */
    public void requireSelfParentOrTeacher(String userId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin() || me.equals(userId) || isParentOf(me, userId)
                || currentUser.hasRole("PROFESSOR") || currentUser.hasRole("TUTOR")) return;
        throw forbidden("Vous ne pouvez consulter que vos propres résultats.");
    }

    /**
     * Mise à jour d'une participation : le professeur responsable (correction) ou l'élève lui-même
     * (soumission), mais l'élève ne peut plus la modifier une fois corrigée par le professeur.
     */
    public void requireCanUpdateParticipation(String utilisateurId, String exerciseProgrammerId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin()) return;
        ExerciseProgrammerEntity ep = getExerciseProgramme(exerciseProgrammerId);
        if (isExerciseProgrammeTeacher(ep, me)) return;
        // L'élève lui-même, ou le parent d'un mineur sans compte qui rend le devoir à sa place.
        if (me.equals(utilisateurId) || canActForMinorChild(utilisateurId)) {
            boolean dejaCorrigee = participerExoRepository
                    .findByUtilisateurIdAndExerciseProgrammerId(utilisateurId, exerciseProgrammerId)
                    .map(p -> p.getEtatSoumission() == cmr.notep.modele.EtatSoumission.CORRIGE)
                    .orElse(false);
            if (!dejaCorrigee) return;
            throw forbidden("Cette copie a déjà été corrigée et ne peut plus être modifiée.");
        }
        throw forbidden("Seul le professeur responsable de cet exercice peut corriger cette copie.");
    }

    /**
     * Utilisateurs "liés" à l'appelant : membres (élèves, parents, enseignants) des classes qu'il
     * gère ou auxquelles il a accès, ses enfants, et lui-même. Sert à filtrer les annuaires
     * (élèves / parents) pour les non-admins au lieu d'exposer tous les comptes de la plateforme.
     */
    public java.util.Set<String> relatedUserIds(String me) {
        java.util.Set<String> classeIds = new java.util.HashSet<>();
        accederRepository.findByUtilisateurId(me).forEach(a -> classeIds.add(a.getClasseId()));
        droitPublicationRepository.findAllClassesByUserId(me).forEach(d -> classeIds.add(d.getClasseId()));
        // (Classes modérées : couvertes par la boucle sur les classes ci-dessous. On évite
        // professeursRepository.findById(me), qui fige le sous-type d'un compte multi-rôles.)
        java.util.Set<String> enfants = new java.util.HashSet<>();
        enfants.addAll(parentEleveRepository.findEleveIdsByParentId(me));
        for (String enfant : enfants) {
            accederRepository.findByUtilisateurId(enfant).forEach(a -> classeIds.add(a.getClasseId()));
        }
        for (ClassesEntity c : classesRepository.findAll()) {
            boolean gere = (c.getModerator() != null && me.equals(c.getModerator().getId()))
                    || me.equals(c.getCreatorId())
                    || isEtablissementGestionnaire(c.getEtablissement(), me);
            if (gere) classeIds.add(c.getId());
        }

        java.util.Set<String> ids = new java.util.HashSet<>(enfants);
        ids.add(me);
        if (!classeIds.isEmpty()) {
            List<String> liste = new java.util.ArrayList<>(classeIds);
            accederRepository.findByClasseIdIn(liste).forEach(a -> ids.add(a.getUtilisateurId()));
            for (String cid : liste) {
                classesRepository.findById(cid).ifPresent(c -> {
                    if (c.getModerator() != null) ids.add(c.getModerator().getId());
                });
                droitPublicationRepository.findAllUsersByClassId(cid).forEach(d -> ids.add(d.getUtilisateurId()));
            }
        }
        // NB : les parents d'élèves ayant eux-mêmes un accès parent à la classe sont déjà inclus.
        return ids;
    }

    public boolean isRelatedUser(String userId) {
        if (currentUser.isAdmin()) return true;
        String me = currentUser.requireUserId();
        return me.equals(userId) || isParentOf(me, userId) || relatedUserIds(me).contains(userId);
    }

    public void requireRelatedUser(String userId) {
        if (!isRelatedUser(userId)) {
            throw forbidden("Vous n'avez pas accès au profil de cet utilisateur.");
        }
    }

    /**
     * Rattacher un enfant à un parent : admin, ou le parent lui-même pour un élève qui n'a encore
     * aucun parent et vient d'être créé (flux "Ajouter un enfant"), afin d'empêcher de s'approprier
     * un élève existant.
     */
    public void requireCanLinkChild(String parentId, String eleveId) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin()) return;
        if (!me.equals(parentId)) {
            throw forbidden("Vous ne pouvez rattacher un enfant qu'à votre propre compte.");
        }
        UtilisateursEntity eleve = utilisateursRepository.findById(required(eleveId, "élève"))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Élève introuvable"));
        boolean dejaRattache = parentEleveRepository.existsOtherParentForEleve(eleveId, me);
        boolean recent = eleve.getCreationDate() != null
                && eleve.getCreationDate().isAfter(java.time.LocalDateTime.now().minusHours(SIGNUP_WINDOW_HOURS));
        if (dejaRattache || !recent) {
            throw forbidden("Cet élève ne peut pas être rattaché à votre compte. Contactez l'administration.");
        }
    }

    public boolean userExists(String userId) {
        return userId != null && utilisateursRepository.existsById(userId);
    }

    // ─── Inscription (appels anonymes) ────────────────────────────────────────

    /** Fenêtre pendant laquelle un compte fraîchement créé peut compléter son inscription sans être connecté. */
    public static final long SIGNUP_WINDOW_HOURS = 24;

    /**
     * Vrai si {@code userId} est un compte professeur en cours d'inscription : jamais activé,
     * pièces justificatives pas encore toutes déposées, créé il y a moins de 24 h.
     * C'est la seule cible autorisée pour les appels ANONYMES de l'inscription
     * (PATCH /utilisateurs/{id}, POST /media/presigned-url, POST /media/proxy-upload), qui exigent EN PLUS
     * le jeton de dépôt émis pour ce compte (voir {@link #hasSignupUploadAccess}).
     */
    /** En-tête portant le jeton de dépôt des pièces renvoyé par l'inscription professeur (POST /utilisateurs). */
    public static final String UPLOAD_TOKEN_HEADER = "X-Upload-Token";

    /**
     * Dépôt ANONYME des pièces justificatives d'un professeur pendant l'inscription : exige le jeton signé
     * renvoyé par POST /utilisateurs (en-tête {@value #UPLOAD_TOKEN_HEADER}, ou {@code Authorization: Bearer}
     * avec ce jeton), émis pour CE compte et non expiré, ET un compte encore en cours d'inscription
     * ({@link #isSignupPendingProfessor}) dont le profil n'est pas validé. Connaître l'id du compte ne suffit plus.
     */
    public boolean hasSignupUploadAccess(String userId) {
        if (userId == null) return false;
        String token = uploadTokenFromCurrentRequest();
        if (!jwtUtil.isValidProfessorDocumentsUploadToken(token, userId)) return false;
        if (!isSignupPendingProfessor(userId)) return false;
        return utilisateursRepository.findStatutVerificationProfesseur(userId)
                .map(cmr.notep.modele.StatutVerificationProfesseur::parse)
                .map(st -> st != cmr.notep.modele.StatutVerificationProfesseur.VALIDE)
                .orElse(true);
    }

    public static String uploadTokenFromCurrentRequest() {
        org.springframework.web.context.request.RequestAttributes attrs =
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra)) return null;
        jakarta.servlet.http.HttpServletRequest request = sra.getRequest();
        String token = request.getHeader(UPLOAD_TOKEN_HEADER);
        if (token != null && !token.isBlank()) return token.trim();
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7).trim();
        return null;
    }

    public boolean isSignupPendingProfessor(String userId) {
        if (userId == null) return false;
        // Requêtes scalaires/natives : le compte peut avoir plusieurs sous-types (multi-rôles).
        if (!utilisateursRepository.hasProfesseurRow(userId)) return false;
        if (utilisateursRepository.professeurHasUploaded(userId)) return false;
        UtilisateursEntity u = utilisateursRepository.findById(userId).orElse(null);
        if (u == null || Boolean.TRUE.equals(u.getAdmin())) return false;
        java.time.LocalDateTime limite = java.time.LocalDateTime.now().minusHours(SIGNUP_WINDOW_HOURS);
        if (u.getEtat() == cmr.notep.modele.EtatUtilisateur.ACTIVE) {
            // Compte existant (parent, élève…) qui vient de demander le rôle professeur via
            // l'inscription : rôle PROFESSOR encore inactif, attribué il y a moins de 24 h.
            return userRoleRepository.findByUtilisateurIdAndRoleType(userId, "PROFESSOR")
                    .filter(r -> !Boolean.TRUE.equals(r.getIsActive()))
                    .map(r -> r.getDateAttribution() != null && r.getDateAttribution().isAfter(limite))
                    .orElse(false);
        }
        return u.getCreationDate() != null && u.getCreationDate().isAfter(limite);
    }
}
