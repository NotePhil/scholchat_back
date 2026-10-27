package cmr.notep.business.services;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.security.AccessControlService;
import cmr.notep.business.security.CurrentUserService;
import cmr.notep.interfaces.modeles.DemandeLienParent;
import cmr.notep.interfaces.modeles.EnfantInscription;
import cmr.notep.interfaces.modeles.EnfantStatut;
import cmr.notep.modele.EtatClasse;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.DemandeLienParentEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.AccederRepository;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.DemandeLienParentRepository;
import cmr.notep.ressourcesjpa.repository.ParentEleveRepository;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Suivi parental d'un enfant MAJEUR (élève qui a son propre compte), PAR CLASSE.
 *
 * <ul>
 *   <li>Demande : e-mail du compte élève de l'enfant + code(s) d'au moins une de ses classes. Aucun compte élève avec
 *       cet e-mail : 404 ELEVE_INTROUVABLE (l'enfant doit d'abord créer son compte ; rien n'est créé) ; compte non
 *       élève : 400 COMPTE_NON_ELEVE ; code inconnu / classe non active / élève non membre de la classe :
 *       400 ELEVE_PAS_DANS_CLASSE (sans révéler ses classes) ; demande déjà en attente ou acceptée pour
 *       (parent, élève, classe) : 409 LIEN_PARENT_DEJA_DEMANDE.</li>
 *   <li>Une demande EN_ATTENTE par classe, envoyée UNIQUEMENT aux gestionnaires de CETTE classe (modérateur, créateur,
 *       co-modérateurs, gestionnaire de l'établissement) : notification PARENT_LINK_REQUEST (CLASS / classeId) +
 *       e-mail au modérateur. Décision par un gestionnaire de la classe ou un administrateur, jamais par l'élève ;
 *       la première décision l'emporte (409 ensuite). Un refus dans une classe n'affecte pas les autres.</li>
 *   <li>Première approbation (toutes classes confondues) : lien parent_eleve créé, l'élève reçoit
 *       PARENT_LINK_ACCEPTED (PARENT_LINK) + e-mail « Votre parent X suit désormais votre scolarité ». Approbations
 *       suivantes : simple notification à l'élève (« … dans la classe Y »). Le parent est notifié de chaque décision
 *       (PARENT_LINK_ACCEPTED / PARENT_LINK_REJECTED, CHILD / eleveId) + e-mail.</li>
 *   <li>La demande ACCEPTEE vaut autorisation de lecture : le parent ne lit les données de l'enfant majeur que pour
 *       ces classes (AccessControlService#classesLisiblesParParent).</li>
 *   <li>Retrait (gestionnaire de la classe ou administrateur ; ni le parent ni l'élève) : demande RETIREE ; quand plus
 *       aucune classe n'est acceptée, le lien parent_eleve est supprimé. Parent et élève notifiés
 *       PARENT_LINK_REMOVED + e-mail.</li>
 *   <li>Le parent peut annuler sa propre demande tant qu'elle est en attente (ANNULEE).</li>
 * </ul>
 */
@Service
@Slf4j
public class LienParentService {

    public static final String TYPE_PARENT_LINK_REQUEST = "PARENT_LINK_REQUEST";
    public static final String TYPE_PARENT_LINK_ACCEPTED = "PARENT_LINK_ACCEPTED";
    public static final String TYPE_PARENT_LINK_REJECTED = "PARENT_LINK_REJECTED";
    public static final String TYPE_PARENT_LINK_REMOVED = "PARENT_LINK_REMOVED";
    /** Notification destinée à l'élève : page « Suivi parental ». */
    public static final String RELATED_PARENT_LINK = "PARENT_LINK";
    /** Notification destinée au parent : « Mes enfants » (relatedEntityId = élève). */
    public static final String RELATED_CHILD = "CHILD";
    /** Notification destinée à un gestionnaire : onglet « Demandes » de la classe (relatedEntityId = classe). */
    public static final String RELATED_CLASS = "CLASS";
    /** Page de l'application où l'élève voit les parents qui suivent sa scolarité (lien des e-mails). */
    public static final String CHEMIN_PAGE_SUIVI = "/schoolchat/demandes-suivi-parental";
    /** Un refus reste visible du parent pendant ce nombre de jours. */
    public static final int JOURS_AFFICHAGE_REFUS = 30;
    public static final int MOTIF_MAX = 500;
    public static final int MAX_CODES = 10;

    public static final String MSG_ELEVE_INTROUVABLE = "Aucun élève avec cette adresse e-mail. Votre enfant majeur doit "
            + "d'abord créer son compte élève ; vous pourrez ensuite demander à suivre sa scolarité.";
    public static final String MSG_ELEVE_PAS_DANS_CLASSE = "Cet élève n'est pas inscrit dans cette classe.";

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final List<String> OUVERTES = List.of(DemandeLienParentEntity.EN_ATTENTE, DemandeLienParentEntity.ACCEPTEE);

    private final DaoAccessorService daoAccessorService;
    private final InscriptionClasseEmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final CurrentUserService currentUser;
    private final AccessControlService accessControl;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    public LienParentService(DaoAccessorService daoAccessorService, InscriptionClasseEmailService emailService,
                             EmailTemplateService emailTemplateService, CurrentUserService currentUser,
                             AccessControlService accessControl) {
        this.daoAccessorService = daoAccessorService;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.currentUser = currentUser;
        this.accessControl = accessControl;
    }

    // ─── Validation (sans écriture) ───────────────────────────────────────────

    /**
     * Valide un enfant majeur (voir la Javadoc de la classe). Renvoie toujours null (pas de classe à rejoindre :
     * l'enfant est déjà membre des classes suivies).
     *
     * @param parentId     null pour un nouveau compte parent
     * @param emailParent  e-mail du parent (l'enfant ne peut pas être le parent lui-même)
     * @param emailsVus    e-mails déjà vus dans la même liste (doublons)
     */
    public ClassesEntity validerEnfantMajeur(String parentId, String emailParent, EnfantInscription e, Integer index,
                                             Set<String> emailsVus) {
        String email = normaliserEmail(e.getEmail());
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw err(SchoolErrorCode.ENFANT_INVALIDE,
                    "Indiquez une adresse e-mail valide pour votre enfant majeur" + suffixe(index) + ".", index);
        }
        if (emailParent != null && email.equalsIgnoreCase(emailParent.trim())) {
            throw err(SchoolErrorCode.ENFANT_INVALIDE,
                    "L'adresse e-mail de votre enfant doit être différente de la vôtre.", index);
        }
        if (emailsVus != null && !emailsVus.add(email)) {
            throw err(SchoolErrorCode.ENFANT_EN_DOUBLE, "L'adresse " + email + " apparaît plusieurs fois dans la liste.", index);
        }
        UtilisateursEntity compte = compteEleve(email, index);
        if (parentId != null && compte.getId().equals(parentId)) {
            throw err(SchoolErrorCode.ENFANT_INVALIDE, "L'adresse e-mail de votre enfant doit être différente de la vôtre.", index);
        }
        resoudreClassesSuivies(parentId, compte, e, index);
        return null;
    }

    private UtilisateursEntity compteEleve(String email, Integer index) {
        UtilisateursEntity compte = trouverCompte(email)
                .orElseThrow(() -> err(SchoolErrorCode.ELEVE_INTROUVABLE, MSG_ELEVE_INTROUVABLE, index));
        if (!daoAccessorService.getRepository(UtilisateursRepository.class).hasEleveRow(compte.getId())) {
            throw err(SchoolErrorCode.COMPTE_NON_ELEVE, "L'adresse " + email + " correspond à un compte ScholChat "
                    + "qui n'est pas un compte élève. Vérifiez l'adresse de votre enfant.", index);
        }
        return compte;
    }

    /** Classes (actives, dont l'élève est membre) désignées par les codes de la demande ; au moins une. */
    private List<ClassesEntity> resoudreClassesSuivies(String parentId, UtilisateursEntity eleve, EnfantInscription e,
                                                      Integer index) {
        LinkedHashSet<String> codes = new LinkedHashSet<>();
        if (e.getCodesClasse() != null) {
            for (String c : e.getCodesClasse()) if (c != null && !c.isBlank()) codes.add(c.trim());
        }
        if (e.getCodeClasse() != null && !e.getCodeClasse().isBlank()) codes.add(e.getCodeClasse().trim());
        if (codes.isEmpty()) {
            throw err(SchoolErrorCode.ENFANT_INVALIDE, "Indiquez le code d'au moins une classe de votre enfant majeur"
                    + suffixe(index) + " : vous suivrez sa scolarité dans les classes indiquées.", index);
        }
        if (codes.size() > MAX_CODES) {
            throw err(SchoolErrorCode.ENFANT_INVALIDE, "Indiquez au plus " + MAX_CODES + " codes de classe.", index);
        }
        ClassesRepository classesRepo = daoAccessorService.getRepository(ClassesRepository.class);
        AccederRepository accederRepo = daoAccessorService.getRepository(AccederRepository.class);
        DemandeLienParentRepository drepo = daoAccessorService.getRepository(DemandeLienParentRepository.class);
        Map<String, ClassesEntity> classes = new LinkedHashMap<>();
        for (String code : codes) {
            ClassesEntity classe = classesRepo.findByCodeActivation(code).stream()
                    .filter(c -> c.getEtat() == EtatClasse.ACTIF)
                    .filter(c -> accederRepo.existsByUtilisateurIdAndClasseId(eleve.getId(), c.getId()))
                    .findFirst()
                    // Même réponse pour un code inconnu, une classe inactive ou une classe dont l'élève n'est pas membre
                    .orElseThrow(() -> err(SchoolErrorCode.ELEVE_PAS_DANS_CLASSE, MSG_ELEVE_PAS_DANS_CLASSE, index));
            classes.putIfAbsent(classe.getId(), classe);
        }
        if (parentId != null) {
            for (ClassesEntity c : classes.values()) {
                if (drepo.existsByParentIdAndEleveIdAndClasseIdAndEtatIn(parentId, eleve.getId(), c.getId(), OUVERTES)) {
                    throw err(SchoolErrorCode.LIEN_PARENT_DEJA_DEMANDE, "Vous suivez déjà " + nom(eleve)
                            + " dans la classe " + c.getNom() + ", ou une demande est déjà en attente pour cette classe.", index);
                }
            }
        }
        return new ArrayList<>(classes.values());
    }

    // ─── Création ─────────────────────────────────────────────────────────────

    /** À appeler après {@link #validerEnfantMajeur}, dans la transaction de l'inscription. */
    public EnfantInscription inscrireEnfantMajeur(String parentId, EnfantInscription demande) {
        UtilisateursEntity parent = daoAccessorService.getRepository(UtilisateursRepository.class).findById(parentId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable"));
        UtilisateursEntity eleve = compteEleve(normaliserEmail(demande.getEmail()), null);
        List<EnfantStatut.ClasseStatut> classesSuivi = new ArrayList<>();
        for (ClassesEntity classe : resoudreClassesSuivies(parentId, eleve, demande, null)) {
            DemandeLienParentEntity d = new DemandeLienParentEntity();
            d.setId(UUID.randomUUID().toString());
            d.setParentId(parentId);
            d.setEleveId(eleve.getId());
            d.setClasseId(classe.getId());
            d.setEtat(DemandeLienParentEntity.EN_ATTENTE);
            d.setDateDemande(LocalDateTime.now());
            daoAccessorService.getRepository(DemandeLienParentRepository.class).save(d);
            notifierGestionnairesDemande(d, parent, eleve, classe);
            classesSuivi.add(EnfantStatut.ClasseStatut.builder()
                    .classeId(classe.getId()).classeNom(classe.getNom())
                    .statut(EnfantStatut.ClasseStatut.EN_ATTENTE).demandeSuiviId(d.getId())
                    .dateDemande(Date.from(d.getDateDemande().atZone(ZoneId.systemDefault()).toInstant()))
                    .build());
            log.info("Parent {}: follow request {} for adult student {} in class {}", parentId, d.getId(), eleve.getId(), classe.getId());
        }
        boolean lie = daoAccessorService.getRepository(ParentEleveRepository.class).existsByParentIdAndEleveId(parentId, eleve.getId());
        return EnfantInscription.builder()
                .id(eleve.getId()).type(EnfantInscription.TYPE_MAJEUR)
                .prenom(eleve.getPrenom()).nom(eleve.getNom()).email(eleve.getEmail())
                .lienStatut(lie ? EnfantStatut.LIEN_LIE : EnfantStatut.LIEN_EN_ATTENTE_VALIDATION_PROFESSEUR)
                .classesSuivi(classesSuivi)
                .build();
    }

    /** POST /parents/{id}/enfants/majeur. */
    @Transactional
    public EnfantInscription ajouterEnfantMajeur(String parentId, EnfantInscription demande) {
        UtilisateursRepository repo = daoAccessorService.getRepository(UtilisateursRepository.class);
        if (parentId == null || !repo.hasParentRow(parentId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable");
        }
        if (demande == null) {
            throw new SchoolException(SchoolErrorCode.ENFANT_INVALIDE, "Indiquez l'adresse e-mail de votre enfant.");
        }
        demande.setType(EnfantInscription.TYPE_MAJEUR);
        String emailParent = repo.findById(parentId).map(UtilisateursEntity::getEmail).orElse(null);
        validerEnfantMajeur(parentId, emailParent, demande, null, new HashSet<>());
        return inscrireEnfantMajeur(parentId, demande);
    }

    // ─── Gestionnaires de classe (professeurs) / administrateur ───────────────

    /** GET /classes/{classeId}/demandes-suivi : demandes en attente pour cette classe (gestionnaire / admin). */
    @Transactional(readOnly = true)
    public List<DemandeLienParent> demandesEnAttenteDeClasse(String classeId) {
        accessControl.getClasse(classeId);
        accessControl.requireClassManager(classeId);
        return versModeles(daoAccessorService.getRepository(DemandeLienParentRepository.class)
                .findByClasseIdAndEtatOrderByDateDemandeAsc(classeId, DemandeLienParentEntity.EN_ATTENTE));
    }

    /** GET /demandes-suivi : toutes les demandes en attente (administrateur). */
    @Transactional(readOnly = true)
    public List<DemandeLienParent> demandesEnAttente() {
        currentUser.requireAdmin();
        return versModeles(daoAccessorService.getRepository(DemandeLienParentRepository.class)
                .findByEtatOrderByDateDemandeAsc(DemandeLienParentEntity.EN_ATTENTE));
    }

    /** GET /classes/{classeId}/suivis-parentaux : suivis acceptés dans cette classe (gestionnaire / admin). */
    @Transactional(readOnly = true)
    public List<DemandeLienParent> suivisDeClasse(String classeId) {
        accessControl.getClasse(classeId);
        accessControl.requireClassManager(classeId);
        return versModeles(daoAccessorService.getRepository(DemandeLienParentRepository.class)
                .findByClasseIdAndEtatOrderByDateDemandeAsc(classeId, DemandeLienParentEntity.ACCEPTEE));
    }

    /**
     * GET /eleves/{eleveId}/suivi-parental : suivis acceptés de l'élève, un par (parent, classe) — l'élève lui-même
     * (lecture seule), un gestionnaire d'une de ses classes, ou un administrateur.
     */
    @Transactional(readOnly = true)
    public List<DemandeLienParent> suiviParentalDeEleve(String eleveId) {
        String me = currentUser.requireUserId();
        if (!me.equals(eleveId) && !currentUser.isAdmin() && !accessControl.isManagerOfStudent(eleveId, me)) {
            throw CurrentUserService.forbidden("Vous ne pouvez consulter que votre propre suivi parental.");
        }
        return versModeles(daoAccessorService.getRepository(DemandeLienParentRepository.class)
                .findByEleveIdAndEtat(eleveId, DemandeLienParentEntity.ACCEPTEE));
    }

    private void requireGestionnaireDeLaDemande(DemandeLienParentEntity d, String message) {
        String me = currentUser.requireUserId();
        if (currentUser.isAdmin()) return;
        if (d.getClasseId() != null && accessControl.isClassManager(d.getClasseId(), me)) return;
        throw CurrentUserService.forbidden(message);
    }

    /** POST /demandes-suivi/{id}/approuver | refuser : gestionnaire de la classe de la demande, ou administrateur. */
    @Transactional
    public DemandeLienParent decider(String demandeId, boolean approuver, String motif) {
        DemandeLienParentRepository repo = daoAccessorService.getRepository(DemandeLienParentRepository.class);
        DemandeLienParentEntity d = repo.findById(demandeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Demande de suivi introuvable"));
        requireGestionnaireDeLaDemande(d, "Seuls les professeurs qui gèrent cette classe (ou un administrateur) "
                + "peuvent décider de cette demande de suivi parental.");
        String me = currentUser.requireUserId();
        String motifPropre = motif == null || motif.isBlank() ? null : motif.trim();
        if (motifPropre != null && motifPropre.length() > MOTIF_MAX) motifPropre = motifPropre.substring(0, MOTIF_MAX);
        int n = repo.deciderSiEnAttente(demandeId, approuver ? DemandeLienParentEntity.ACCEPTEE : DemandeLienParentEntity.REFUSEE,
                LocalDateTime.now(), approuver ? null : motifPropre, me);
        if (n == 0) {
            throw new SchoolException(SchoolErrorCode.CONFLICT, "Cette demande de suivi a déjà été traitée.");
        }
        d = repo.findById(demandeId).orElseThrow();
        UtilisateursRepository urepo = daoAccessorService.getRepository(UtilisateursRepository.class);
        UtilisateursEntity eleve = urepo.findById(d.getEleveId()).orElseThrow();
        UtilisateursEntity parent = urepo.findById(d.getParentId()).orElse(null);
        String nomEleve = nom(eleve);
        String nomParent = parent != null ? nom(parent) : "Un parent";
        String classeNom = nomClasse(d.getClasseId());
        if (approuver) {
            ParentEleveRepository liens = daoAccessorService.getRepository(ParentEleveRepository.class);
            boolean premier = !liens.existsByParentIdAndEleveId(d.getParentId(), d.getEleveId());
            if (premier) {
                liens.insertLien(d.getParentId(), d.getEleveId());
                notifier(eleve.getId(), TYPE_PARENT_LINK_ACCEPTED, "Suivi parental",
                        "Votre parent " + nomParent + " suit désormais votre scolarité sur ScholChat (classe " + classeNom
                                + ", lecture seule).", d.getParentId(), nomParent, d.getId(), RELATED_PARENT_LINK);
                envoyerEmail(eleve.getEmail(), "Votre parent " + nomParent + " suit désormais votre scolarité",
                        "Suivi parental",
                        List.of("Bonjour " + safe(eleve.getPrenom()) + ",",
                                "Votre parent " + nomParent + (parent != null && parent.getEmail() != null ? " (" + parent.getEmail() + ")" : "")
                                        + " suit désormais votre scolarité sur ScholChat dans la classe " + classeNom
                                        + " : cours, progression et devoirs de cette classe, en lecture seule (il ne peut "
                                        + "pas agir à votre place).",
                                "Ce suivi a été validé par votre professeur. Si vous pensez qu'il s'agit d'une erreur, "
                                        + "contactez votre professeur ou l'administration."),
                        CHEMIN_PAGE_SUIVI, "Voir mon suivi parental");
            } else {
                notifier(eleve.getId(), TYPE_PARENT_LINK_ACCEPTED, "Suivi parental",
                        "Votre parent " + nomParent + " suit aussi votre scolarité dans la classe " + classeNom + ".",
                        d.getParentId(), nomParent, d.getId(), RELATED_PARENT_LINK);
            }
            if (parent != null) {
                notifier(parent.getId(), TYPE_PARENT_LINK_ACCEPTED, "Demande de suivi acceptée",
                        "Votre demande de suivi de " + nomEleve + " dans la classe " + classeNom + " a été acceptée. "
                                + "Retrouvez-le dans « Mes enfants » (lecture seule).", eleve.getId(), nomEleve, eleve.getId(), RELATED_CHILD);
                envoyerEmail(parent.getEmail(), "Suivi de " + nomEleve + " accepté – classe " + classeNom,
                        "Demande de suivi acceptée",
                        List.of("Bonjour " + safe(parent.getPrenom()) + ",",
                                "Le professeur de la classe " + classeNom + " a accepté votre demande de suivi de " + nomEleve
                                        + ". Retrouvez ses cours, sa progression et ses devoirs de cette classe dans « Mes "
                                        + "enfants » (lecture seule : votre enfant agit lui-même depuis son compte)."),
                        null, null);
            }
        } else if (parent != null) {
            String finMotif = motifPropre != null ? " Motif : " + motifPropre : "";
            notifier(parent.getId(), TYPE_PARENT_LINK_REJECTED, "Demande de suivi refusée",
                    "Votre demande de suivi de " + nomEleve + " dans la classe " + classeNom + " a été refusée." + finMotif,
                    eleve.getId(), nomEleve, eleve.getId(), RELATED_CHILD);
            List<String> paragraphes = new ArrayList<>(List.of("Bonjour " + safe(parent.getPrenom()) + ",",
                    "Votre demande de suivi de la scolarité de " + nomEleve + " dans la classe " + classeNom
                            + " a été refusée par le professeur de la classe."));
            if (motifPropre != null) paragraphes.add("Motif : " + motifPropre);
            envoyerEmail(parent.getEmail(), "Suivi de " + nomEleve + " refusé – classe " + classeNom,
                    "Demande de suivi refusée", paragraphes, null, null);
        }
        log.info("User {} {} parent follow request {} (parent {}, student {}, class {})", me,
                approuver ? "approved" : "refused", demandeId, d.getParentId(), d.getEleveId(), d.getClasseId());
        return versModele(d, parent, eleve);
    }

    /** POST /demandes-suivi/{id}/annuler : le parent annule sa propre demande encore en attente. */
    @Transactional
    public DemandeLienParent annulerParParent(String demandeId) {
        String me = currentUser.requireUserId();
        DemandeLienParentRepository repo = daoAccessorService.getRepository(DemandeLienParentRepository.class);
        DemandeLienParentEntity d = repo.findById(demandeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Demande de suivi introuvable"));
        if (!me.equals(d.getParentId()) && !currentUser.isAdmin()) {
            throw CurrentUserService.forbidden("Seul le parent qui a envoyé la demande peut l'annuler.");
        }
        if (repo.deciderSiEnAttente(demandeId, DemandeLienParentEntity.ANNULEE, LocalDateTime.now(),
                "Annulée par le parent", me) == 0) {
            throw new SchoolException(SchoolErrorCode.CONFLICT, "Cette demande de suivi a déjà été traitée.");
        }
        log.info("Parent {} cancelled follow request {}", me, demandeId);
        return versModeles(List.of(repo.findById(demandeId).orElseThrow())).get(0);
    }

    /** DELETE /demandes-suivi/{id} : retire un suivi accepté (gestionnaire de sa classe ou administrateur). */
    @Transactional
    public void retirerSuivi(String demandeId) {
        DemandeLienParentRepository repo = daoAccessorService.getRepository(DemandeLienParentRepository.class);
        DemandeLienParentEntity d = repo.findById(demandeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Suivi parental introuvable"));
        requireGestionnaireDeLaDemande(d, "Seuls les professeurs qui gèrent cette classe (ou un administrateur) "
                + "peuvent retirer ce suivi parental.");
        retirer(d);
    }

    /** DELETE /classes/{classeId}/suivis/{parentId}/{eleveId}. */
    @Transactional
    public void retirerSuiviDeClasse(String classeId, String parentId, String eleveId) {
        accessControl.getClasse(classeId);
        accessControl.requireClassManager(classeId);
        DemandeLienParentEntity d = daoAccessorService.getRepository(DemandeLienParentRepository.class)
                .findByParentIdAndEleveIdAndEtat(parentId, eleveId, DemandeLienParentEntity.ACCEPTEE).stream()
                .filter(x -> classeId.equals(x.getClasseId())).findFirst()
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Ce parent ne suit pas cet élève dans cette classe."));
        retirer(d);
    }

    /**
     * DELETE /parents/{parentId}/enfants/{eleveId} pour un élève majeur : administrateur → tous les suivis (et le
     * lien) ; gestionnaire → les suivis des classes qu'il gère. Ni le parent ni l'élève.
     */
    @Transactional
    public void retirerSuivisParentEleve(String parentId, String eleveId) {
        String me = currentUser.requireUserId();
        boolean admin = currentUser.isAdmin();
        if (!admin && !accessControl.isManagerOfStudent(eleveId, me)) {
            throw CurrentUserService.forbidden("Seuls les professeurs qui gèrent une classe de l'élève (ou un "
                    + "administrateur) peuvent retirer le suivi parental d'un élève majeur.");
        }
        ParentEleveRepository liens = daoAccessorService.getRepository(ParentEleveRepository.class);
        List<DemandeLienParentEntity> acceptees = daoAccessorService.getRepository(DemandeLienParentRepository.class)
                .findByParentIdAndEleveIdAndEtat(parentId, eleveId, DemandeLienParentEntity.ACCEPTEE).stream()
                .filter(d -> admin || (d.getClasseId() != null && accessControl.isClassManager(d.getClasseId(), me)))
                .toList();
        if (acceptees.isEmpty()) {
            if (admin && liens.existsByParentIdAndEleveId(parentId, eleveId)) {
                liens.deleteLien(parentId, eleveId); // ancien lien sans suivi par classe
                return;
            }
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Ce parent ne suit pas cet élève dans vos classes.");
        }
        acceptees.forEach(this::retirer);
    }

    private void retirer(DemandeLienParentEntity d) {
        String me = currentUser.requireUserId();
        DemandeLienParentRepository repo = daoAccessorService.getRepository(DemandeLienParentRepository.class);
        if (repo.retirerSiAcceptee(d.getId(), LocalDateTime.now(), "Suivi retiré", me) == 0) {
            throw new SchoolException(SchoolErrorCode.CONFLICT, "Ce suivi parental n'est plus actif.");
        }
        ParentEleveRepository liens = daoAccessorService.getRepository(ParentEleveRepository.class);
        boolean dernier = repo.countByParentIdAndEleveIdAndEtat(d.getParentId(), d.getEleveId(), DemandeLienParentEntity.ACCEPTEE) == 0;
        if (dernier) liens.deleteLien(d.getParentId(), d.getEleveId());
        UtilisateursRepository urepo = daoAccessorService.getRepository(UtilisateursRepository.class);
        UtilisateursEntity eleve = urepo.findById(d.getEleveId()).orElse(null);
        UtilisateursEntity parent = urepo.findById(d.getParentId()).orElse(null);
        String nomEleve = eleve != null ? nom(eleve) : "votre enfant";
        String nomParent = parent != null ? nom(parent) : "Votre parent";
        String classeNom = nomClasse(d.getClasseId());
        if (parent != null) {
            String msg = "Le suivi de " + nomEleve + " dans la classe " + classeNom + " a été retiré par le professeur."
                    + (dernier ? " " + nomEleve + " n'apparaît plus dans « Mes enfants »." : "");
            notifier(parent.getId(), TYPE_PARENT_LINK_REMOVED, "Suivi parental retiré", msg,
                    d.getEleveId(), nomEleve, d.getEleveId(), RELATED_CHILD);
            envoyerEmail(parent.getEmail(), "Suivi de " + nomEleve + " retiré – classe " + classeNom,
                    "Suivi parental retiré", List.of("Bonjour " + safe(parent.getPrenom()) + ",", msg), null, null);
        }
        if (eleve != null) {
            String msg = dernier ? nomParent + " ne suit plus votre scolarité sur ScholChat."
                    : nomParent + " ne suit plus votre scolarité dans la classe " + classeNom + ".";
            notifier(eleve.getId(), TYPE_PARENT_LINK_REMOVED, "Suivi parental retiré", msg,
                    d.getParentId(), nomParent, d.getId(), RELATED_PARENT_LINK);
            envoyerEmail(eleve.getEmail(), dernier ? nomParent + " ne suit plus votre scolarité"
                            : nomParent + " ne suit plus la classe " + classeNom, "Suivi parental retiré",
                    List.of("Bonjour " + safe(eleve.getPrenom()) + ",", msg + " Ce suivi a été retiré par votre professeur."),
                    CHEMIN_PAGE_SUIVI, "Voir mon suivi parental");
        }
        log.info("User {} removed parent follow {} (parent {}, student {}, class {}, link removed: {})", me, d.getId(),
                d.getParentId(), d.getEleveId(), d.getClasseId(), dernier);
    }

    // ─── Lecture pour le parent ───────────────────────────────────────────────

    /**
     * Demandes de suivi d'un parent à afficher (GET /parents/{id}/enfants/statuts) : la plus récente par (élève,
     * classe), en attente, acceptée, ou refusée depuis moins de {@link #JOURS_AFFICHAGE_REFUS} jours.
     */
    public List<DemandeLienParentEntity> demandesSuiviDuParent(String parentId) {
        LocalDateTime limiteRefus = LocalDateTime.now().minusDays(JOURS_AFFICHAGE_REFUS);
        Set<String> vus = new HashSet<>();
        List<DemandeLienParentEntity> res = new ArrayList<>();
        for (DemandeLienParentEntity d : daoAccessorService.getRepository(DemandeLienParentRepository.class)
                .findByParentIdOrderByDateDemandeDesc(parentId)) {
            if (d.getClasseId() == null || !vus.add(d.getEleveId() + "|" + d.getClasseId())) continue;
            boolean visible = OUVERTES.contains(d.getEtat())
                    || (DemandeLienParentEntity.REFUSEE.equals(d.getEtat())
                        && d.getDateReponse() != null && d.getDateReponse().isAfter(limiteRefus));
            if (visible) res.add(d);
        }
        return res;
    }

    public String nomClasse(String classeId) {
        if (classeId == null) return "?";
        return daoAccessorService.getRepository(ClassesRepository.class).findById(classeId)
                .map(ClassesEntity::getNom).orElse("?");
    }

    // ─── Notifications / e-mails ──────────────────────────────────────────────

    /** Gestionnaires de LA classe de la demande : notification PARENT_LINK_REQUEST (CLASS / classeId) ; e-mail au modérateur. */
    private void notifierGestionnairesDemande(DemandeLienParentEntity d, UtilisateursEntity parent, UtilisateursEntity eleve,
                                              ClassesEntity classe) {
        String nomParent = nom(parent);
        String nomEleve = nom(eleve);
        String message = "Demande de suivi parental — " + nomParent + " souhaite suivre " + nomEleve + " (classe "
                + classe.getNom() + "). Approuvez ou refusez depuis l'onglet « Demandes » de la classe.";
        for (String g : gestionnaires(classe)) {
            if (g.equals(eleve.getId()) || g.equals(parent.getId())) continue;
            notifier(g, TYPE_PARENT_LINK_REQUEST, "Demande de suivi parental", message, parent.getId(), nomParent,
                    classe.getId(), RELATED_CLASS);
        }
        UtilisateursEntity moderateur = classe.getModerator();
        if (moderateur != null && moderateur.getEmail() != null && !moderateur.getId().equals(parent.getId())) {
            envoyerEmail(moderateur.getEmail(), "Demande de suivi parental – " + nomEleve + " (classe " + classe.getNom() + ")",
                    "Demande de suivi parental",
                    List.of("Bonjour " + safe(moderateur.getPrenom()) + ",",
                            nomParent + (parent.getEmail() != null ? " (" + parent.getEmail() + ")" : "")
                                    + " souhaite suivre la scolarité de " + nomEleve + ", élève majeur de votre classe "
                                    + classe.getNom() + " (lecture seule : cours, progression et devoirs de cette classe).",
                            "Approuvez ou refusez cette demande depuis l'onglet « Demandes » de la classe."),
                    null, null);
        }
    }

    /** Modérateur, créateur, co-modérateurs et gestionnaire de l'établissement d'une classe. */
    private Set<String> gestionnaires(ClassesEntity classe) {
        Set<String> ids = new LinkedHashSet<>();
        if (classe.getModerator() != null) ids.add(classe.getModerator().getId());
        if (classe.getCreatorId() != null) ids.add(classe.getCreatorId());
        if (classe.getEtablissement() != null && classe.getEtablissement().getGestionnaire() != null) {
            ids.add(classe.getEtablissement().getGestionnaire().getId());
        }
        try {
            ids.addAll(daoAccessorService.getRepository(ProfesseursRepository.class).findModeratorIdsForClass(classe.getId()));
        } catch (Exception ignored) {
            // table de co-modération absente
        }
        ids.remove(null);
        return ids;
    }

    private void envoyerEmail(String email, String sujet, String titre, List<String> paragraphes, String chemin, String bouton) {
        if (email == null || email.isBlank()) return;
        try {
            String html = emailTemplateService.generateNotificationGeneriqueEmail(titre, paragraphes, null, chemin, bouton);
            InscriptionClasseService.apresCommit(() -> emailService.envoyerHtml(email, sujet, html));
        } catch (Exception e) {
            log.error("Parent-link e-mail could not be prepared for {}: {}", email, e.getMessage());
        }
    }

    private void notifier(String userId, String type, String titre, String message, String actorId, String actorName,
                          String relatedId, String relatedType) {
        try {
            notificationService.createNotification(userId, type, titre, message, actorId, actorName, relatedId, relatedType);
        } catch (Exception e) {
            log.error("Parent-link notification could not be created for {}: {}", userId, e.getMessage());
        }
    }

    // ─── Utilitaires ──────────────────────────────────────────────────────────

    private Optional<UtilisateursEntity> trouverCompte(String email) {
        if (email == null) return Optional.empty();
        UtilisateursRepository repo = daoAccessorService.getRepository(UtilisateursRepository.class);
        Optional<UtilisateursEntity> u = repo.findByEmail(email);
        return u.isPresent() ? u : repo.findFirstByEmailIgnoreCase(email);
    }

    private List<DemandeLienParent> versModeles(List<DemandeLienParentEntity> demandes) {
        UtilisateursRepository urepo = daoAccessorService.getRepository(UtilisateursRepository.class);
        Map<String, UtilisateursEntity> cache = new HashMap<>();
        List<DemandeLienParent> res = new ArrayList<>();
        for (DemandeLienParentEntity d : demandes) {
            UtilisateursEntity parent = cache.computeIfAbsent(d.getParentId(), id -> urepo.findById(id).orElse(null));
            UtilisateursEntity eleve = cache.computeIfAbsent(d.getEleveId(), id -> urepo.findById(id).orElse(null));
            res.add(versModele(d, parent, eleve));
        }
        return res;
    }

    private DemandeLienParent versModele(DemandeLienParentEntity d, UtilisateursEntity parent, UtilisateursEntity eleve) {
        return DemandeLienParent.builder()
                .id(d.getId()).parentId(d.getParentId()).eleveId(d.getEleveId())
                .parentPrenom(parent != null ? parent.getPrenom() : null)
                .parentNom(parent != null ? parent.getNom() : null)
                .parentEmail(parent != null ? parent.getEmail() : null)
                .elevePrenom(eleve != null ? eleve.getPrenom() : null)
                .eleveNom(eleve != null ? eleve.getNom() : null)
                .eleveEmail(eleve != null ? eleve.getEmail() : null)
                .classeId(d.getClasseId())
                .classeNom(d.getClasseId() != null ? nomClasse(d.getClasseId()) : null)
                .etat(d.getEtat()).dateDemande(d.getDateDemande()).dateReponse(d.getDateReponse())
                .depuis(DemandeLienParentEntity.ACCEPTEE.equals(d.getEtat()) ? d.getDateReponse() : null)
                .motif(DemandeLienParentEntity.REFUSEE.equals(d.getEtat()) ? d.getMotif() : null)
                .build();
    }

    public static String normaliserEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static SchoolException err(SchoolErrorCode code, String message, Integer index) {
        return new SchoolException(code, message).avecEnfantIndex(index);
    }

    private static String suffixe(Integer index) {
        return index != null ? " (enfant n°" + (index + 1) + ")" : "";
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    static String nom(UtilisateursEntity u) {
        return (safe(u.getPrenom()) + " " + safe(u.getNom())).trim();
    }
}
