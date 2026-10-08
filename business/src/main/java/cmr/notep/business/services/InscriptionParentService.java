package cmr.notep.business.services;

import cmr.notep.business.business.AccederBusiness;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.EnfantInscription;
import cmr.notep.interfaces.modeles.EnfantStatut;
import cmr.notep.modele.EtatDemandeAcces;
import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.AccederEntity;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.DemandeAccesEntity;
import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.AccederRepository;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.DemandeAccesRepository;
import cmr.notep.ressourcesjpa.repository.ElevesRepository;
import cmr.notep.ressourcesjpa.repository.ParentEleveRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Inscription d'un parent avec ses enfants (chaque enfant avec le code de sa classe).
 *
 * <ol>
 *   <li>POST /utilisateurs {type: "parent", …, enfants: [{prenom, nom, codeClasse}]} : tout est validé avant
 *       d'écrire (e-mail, enfants, codes). Puis, dans une seule transaction : compte parent ACTIF avec un mot de
 *       passe temporaire (must_change_password), rôle PARENT actif ; pour chaque enfant : élève mineur (sans
 *       e-mail ni mot de passe, niveau = celui de la classe), lien parent_eleve, demande d'accès
 *       (utilisateur = parent, estParent, eleveAssocieId = enfant) EN_ATTENTE, modérateur notifié. Après commit,
 *       un seul e-mail au parent : identifiants + liste des enfants (en attente de validation).</li>
 *   <li>Tant qu'aucun enfant n'est accepté, une session PARENT est limitée (ParentSansEnfantValide).</li>
 *   <li>Approbation d'une demande pour un enfant : accès de l'enfant ET du parent à la classe (AccederBusiness),
 *       e-mail + notification CHILD_ACCESS_APPROVED au parent ; refus : e-mail avec le motif + notification
 *       CHILD_ACCESS_REJECTED.</li>
 * </ol>
 */
@Service
@Slf4j
public class InscriptionParentService {

    public static final String STATUT_COMPTE_PARENT_CREE = "COMPTE_PARENT_CREE";
    public static final int MAX_ENFANTS = 10;

    /** Notification au parent : enfant accepté dans une classe (relatedEntity CLASS = classeId, actorId = enfant). */
    public static final String TYPE_CHILD_ACCESS_APPROVED = "CHILD_ACCESS_APPROVED";
    /** Notification au parent : demande pour un enfant refusée (relatedEntity CLASS = classeId, actorId = enfant). */
    public static final String TYPE_CHILD_ACCESS_REJECTED = "CHILD_ACCESS_REJECTED";

    private final DaoAccessorService daoAccessorService;
    private final InscriptionClasseService inscriptionClasseService;
    private final InscriptionClasseEmailService emailService;

    @Autowired
    @Lazy
    private AccederBusiness accederBusiness;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    public InscriptionParentService(DaoAccessorService daoAccessorService,
                                    InscriptionClasseService inscriptionClasseService,
                                    InscriptionClasseEmailService emailService) {
        this.daoAccessorService = daoAccessorService;
        this.inscriptionClasseService = inscriptionClasseService;
        this.emailService = emailService;
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    /**
     * Valide la liste des enfants d'une inscription parent, SANS rien écrire : au moins un enfant (au plus
     * {@link #MAX_ENFANTS}), prénom et nom renseignés, pas de doublon (même prénom + nom) dans la liste ni parmi
     * les enfants déjà rattachés au parent, code de classe valide (classe ACTIF, non expirée).
     *
     * @param nomsExistants clés {@link #cleNom} des enfants déjà rattachés au parent (vide pour un nouveau compte)
     * @return la classe de chaque enfant, dans l'ordre de la liste
     */
    public List<ClassesEntity> validerEnfants(List<EnfantInscription> enfants, Set<String> nomsExistants) {
        if (enfants == null || enfants.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.ENFANTS_REQUIS,
                    "Ajoutez au moins un enfant (prénom, nom et code de sa classe) pour créer votre compte parent.");
        }
        if (enfants.size() > MAX_ENFANTS) {
            throw new SchoolException(SchoolErrorCode.ENFANTS_TROP_NOMBREUX,
                    "Vous pouvez inscrire au plus " + MAX_ENFANTS + " enfants à la fois.");
        }
        List<ClassesEntity> classes = new ArrayList<>();
        Set<String> vus = new HashSet<>();
        for (int i = 0; i < enfants.size(); i++) {
            classes.add(validerEnfant(enfants.get(i), i, vus, nomsExistants));
        }
        return classes;
    }

    private ClassesEntity validerEnfant(EnfantInscription e, Integer index, Set<String> vus, Set<String> nomsExistants) {
        if (e == null || estVide(e.getPrenom()) || estVide(e.getNom())) {
            throw new SchoolException(SchoolErrorCode.ENFANT_INVALIDE,
                    "Indiquez le prénom et le nom de l'enfant" + (index != null ? " n°" + (index + 1) : "") + ".")
                    .avecEnfantIndex(index);
        }
        if (e.getPrenom().trim().length() > 100 || e.getNom().trim().length() > 100) {
            throw new SchoolException(SchoolErrorCode.ENFANT_INVALIDE,
                    "Le prénom et le nom de l'enfant ne doivent pas dépasser 100 caractères.").avecEnfantIndex(index);
        }
        String cle = cleNom(e.getPrenom(), e.getNom());
        String libelle = e.getPrenom().trim() + " " + e.getNom().trim();
        if (nomsExistants != null && nomsExistants.contains(cle)) {
            throw new SchoolException(SchoolErrorCode.ENFANT_EN_DOUBLE,
                    "Un enfant nommé " + libelle + " est déjà rattaché à votre compte. Pour l'inscrire dans une autre "
                            + "classe, faites une demande d'accès pour cet enfant depuis votre espace parent.")
                    .avecEnfantIndex(index);
        }
        if (!vus.add(cle)) {
            throw new SchoolException(SchoolErrorCode.ENFANT_EN_DOUBLE,
                    "L'enfant " + libelle + " apparaît plusieurs fois dans la liste.").avecEnfantIndex(index);
        }
        try {
            // Enfant (mineur ou non) : toute classe ACTIF non expirée, sans restriction majeurs / mineurs.
            return inscriptionClasseService.resoudreClasse(e.getCodeClasse(), false);
        } catch (SchoolException ex) {
            throw new SchoolException(ex.getCode(), ex.getMessage()).avecEnfantIndex(index);
        }
    }

    /** Clé de comparaison prénom + nom (casse, accents et espaces ignorés). */
    public static String cleNom(String prenom, String nom) {
        return normaliser(prenom) + "|" + normaliser(nom);
    }

    private static String normaliser(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return n.replaceAll("\\s+", " ");
    }

    private static boolean estVide(String s) {
        return s == null || s.isBlank();
    }

    /** Clés {@link #cleNom} des enfants déjà rattachés au parent. */
    public Set<String> nomsEnfantsExistants(String parentId) {
        Set<String> noms = new HashSet<>();
        for (ElevesEntity e : enfantsDe(parentId)) {
            noms.add(cleNom(e.getPrenom(), e.getNom()));
        }
        return noms;
    }

    // ─── Création ─────────────────────────────────────────────────────────────

    /**
     * Crée l'élève (mineur, sans identifiants), le rattache au parent et envoie la demande d'accès à sa classe
     * (le modérateur est notifié). À appeler dans la transaction de l'inscription, après validation.
     */
    public EnfantInscription inscrireEnfant(String parentId, EnfantInscription demande, ClassesEntity classe) {
        ElevesEntity eleve = new ElevesEntity();
        eleve.setId(UUID.randomUUID().toString());
        eleve.setPrenom(demande.getPrenom().trim());
        eleve.setNom(demande.getNom().trim());
        eleve.setNiveau(classe.getNiveau() != null && !classe.getNiveau().isBlank() ? classe.getNiveau() : "Non précisé");
        eleve.setEmail(null);
        eleve.setPasseAccess(null);
        eleve.setActivationToken(null);
        eleve.setResetPasswordToken(null);
        eleve.setMustChangePassword(false);
        eleve.setAdmin(false);
        // Activé à la première approbation (AccederBusiness#validerDemandeAcces). Jamais PENDING (purge).
        eleve.setEtat(EtatUtilisateur.INACTIVE);
        eleve.setCreationDate(LocalDateTime.now());
        ElevesEntity cree = daoAccessorService.getRepository(ElevesRepository.class).save(eleve);

        daoAccessorService.getRepository(ParentEleveRepository.class).insertLien(parentId, cree.getId());
        accederBusiness.demanderAcces(parentId, classe.getId(), classe.getCodeActivation(), true, cree.getId());
        log.info("Parent {}: child {} created and access request sent to class {}", parentId, cree.getId(), classe.getId());

        return EnfantInscription.builder()
                .id(cree.getId())
                .prenom(cree.getPrenom())
                .nom(cree.getNom())
                .niveau(cree.getNiveau())
                .classeId(classe.getId())
                .classeNom(classe.getNom())
                .statut(EnfantStatut.ClasseStatut.EN_ATTENTE)
                .build();
    }

    /**
     * Mot de passe temporaire du nouveau compte parent (même générateur que l'inscription par code de classe).
     * Il n'apparaît que dans l'e-mail, jamais dans les journaux.
     */
    public String genererMotDePasseTemporaire() {
        return inscriptionClasseService.genererMotDePasseTemporaire();
    }

    /** Après commit : e-mail unique au parent (identifiants + enfants en attente de validation). */
    public void envoyerEmailInscriptionApresCommit(String email, String nomComplet, String motDePasse,
                                                    List<EnfantInscription> enfants) {
        List<Map<String, String>> lignes = new ArrayList<>();
        for (EnfantInscription e : enfants) {
            Map<String, String> l = new LinkedHashMap<>();
            l.put("nom", (e.getPrenom() + " " + e.getNom()).trim());
            l.put("classe", e.getClasseNom());
            lignes.add(l);
        }
        InscriptionClasseService.apresCommit(() -> emailService.envoyerInscriptionParentRecue(email, nomComplet, motDePasse, lignes));
    }

    /**
     * POST /parents/{id}/enfants/inscription : nouvel enfant + demande d'accès à sa classe (parent connecté).
     * Un enfant déjà rattaché qui rejoint une autre classe passe par POST /acceder/demandes (eleveAssocieId).
     */
    @Transactional
    public EnfantInscription inscrireNouvelEnfant(String parentId, EnfantInscription demande) {
        if (parentId == null || !daoAccessorService.getRepository(UtilisateursRepository.class).hasParentRow(parentId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable");
        }
        if (demande == null) {
            throw new SchoolException(SchoolErrorCode.ENFANT_INVALIDE, "Indiquez le prénom et le nom de l'enfant.");
        }
        ClassesEntity classe = validerEnfant(demande, null, new HashSet<>(), nomsEnfantsExistants(parentId));
        return inscrireEnfant(parentId, demande, classe);
    }

    // ─── Statuts ──────────────────────────────────────────────────────────────

    /** GET /parents/{id}/enfants/statuts : chaque enfant et l'état de ses inscriptions (accès + demandes). */
    @Transactional(readOnly = true)
    public List<EnfantStatut> statuts(String parentId) {
        if (parentId == null || !daoAccessorService.getRepository(UtilisateursRepository.class).hasParentRow(parentId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent introuvable");
        }
        List<ElevesEntity> enfants = enfantsDe(parentId);
        if (enfants.isEmpty()) return Collections.emptyList();

        List<String> ids = enfants.stream().map(ElevesEntity::getId).toList();
        Map<String, List<DemandeAccesEntity>> demandesParEnfant = new HashMap<>();
        for (DemandeAccesEntity d : daoAccessorService.getRepository(DemandeAccesRepository.class).findByEleveAssocieIdIn(ids)) {
            demandesParEnfant.computeIfAbsent(d.getEleveAssocieId(), k -> new ArrayList<>()).add(d);
        }
        AccederRepository accederRepo = daoAccessorService.getRepository(AccederRepository.class);
        ClassesRepository classesRepo = daoAccessorService.getRepository(ClassesRepository.class);

        List<EnfantStatut> resultat = new ArrayList<>();
        for (ElevesEntity enfant : enfants) {
            List<DemandeAccesEntity> demandes = new ArrayList<>(demandesParEnfant.getOrDefault(enfant.getId(), List.of()));
            // Plus récente d'abord
            demandes.sort(Comparator.comparing(DemandeAccesEntity::getDateDemande,
                    Comparator.nullsLast(Comparator.reverseOrder())));
            Map<String, EnfantStatut.ClasseStatut> parClasse = new LinkedHashMap<>();
            for (AccederEntity a : accederRepo.findByUtilisateurId(enfant.getId())) {
                Date date = demandes.stream()
                        .filter(d -> d.getClasse() != null && a.getClasseId().equals(d.getClasse().getId())
                                && d.getEtat() == EtatDemandeAcces.APPROUVEE)
                        .map(DemandeAccesEntity::getDateDemande).findFirst().orElse(null);
                String classeNom = a.getClasse() != null ? a.getClasse().getNom()
                        : classesRepo.findById(a.getClasseId()).map(ClassesEntity::getNom).orElse(null);
                parClasse.put(a.getClasseId(), EnfantStatut.ClasseStatut.builder()
                        .classeId(a.getClasseId()).classeNom(classeNom)
                        .statut(EnfantStatut.ClasseStatut.APPROUVEE).dateDemande(date).build());
            }
            for (DemandeAccesEntity d : demandes) {
                if (d.getClasse() == null || parClasse.containsKey(d.getClasse().getId())) continue;
                // Demande approuvée dont l'accès a été retiré depuis : rien à afficher
                if (d.getEtat() != EtatDemandeAcces.EN_ATTENTE && d.getEtat() != EtatDemandeAcces.REJETEE) continue;
                boolean rejetee = d.getEtat() == EtatDemandeAcces.REJETEE;
                parClasse.put(d.getClasse().getId(), EnfantStatut.ClasseStatut.builder()
                        .classeId(d.getClasse().getId()).classeNom(d.getClasse().getNom())
                        .statut(rejetee ? EnfantStatut.ClasseStatut.REJETEE : EnfantStatut.ClasseStatut.EN_ATTENTE)
                        .motifRejet(rejetee ? d.getMotifRejet() : null)
                        .dateDemande(d.getDateDemande()).build());
            }
            resultat.add(EnfantStatut.builder()
                    .enfantId(enfant.getId())
                    .prenom(enfant.getPrenom())
                    .nom(enfant.getNom())
                    .niveau(enfant.getNiveau())
                    .classes(new ArrayList<>(parClasse.values()))
                    .build());
        }
        return resultat;
    }

    /** Au moins un enfant accepté dans une classe (voir ParentEleveRepository#parentAEnfantValide). */
    public boolean parentAEnfantValide(String parentId) {
        return parentId != null && daoAccessorService.getRepository(ParentEleveRepository.class).parentAEnfantValide(parentId);
    }

    // ─── Décisions du professeur ──────────────────────────────────────────────

    /** Demande faite par un parent pour un de ses enfants (estParent + eleveAssocieId). */
    public static boolean estDemandePourEnfant(DemandeAccesEntity demande) {
        return demande != null && demande.isEstParent() && demande.getEleveAssocieId() != null
                && !demande.getEleveAssocieId().isBlank();
    }

    /**
     * Approbation d'une demande pour un enfant : notification CHILD_ACCESS_APPROVED (relatedEntityType CLASS,
     * relatedEntityId = classeId, actorId = id de l'enfant, actorName = nom de l'enfant) et e-mail au parent
     * (après commit). Les accès (enfant + parent) sont accordés par AccederBusiness.
     */
    public void notifierEnfantAccepte(DemandeAccesEntity demande) {
        UtilisateursEntity parent = demande.getUtilisateur();
        ClassesEntity classe = demande.getClasse();
        String enfantNom = nomEnfant(demande.getEleveAssocieId());
        notifier(parent.getId(), TYPE_CHILD_ACCESS_APPROVED, "Enfant accepté dans une classe",
                "Votre enfant " + enfantNom + " a été accepté dans la classe " + classe.getNom() + ".",
                demande.getEleveAssocieId(), enfantNom, classe.getId());
        if (parent.getEmail() != null) {
            String email = parent.getEmail();
            String nomParent = nomComplet(parent);
            String classeNom = classe.getNom();
            InscriptionClasseService.apresCommit(() -> emailService.envoyerEnfantAccepte(email, nomParent, enfantNom, classeNom));
        }
    }

    /** Refus d'une demande pour un enfant : notification CHILD_ACCESS_REJECTED + e-mail avec le motif. */
    public void notifierEnfantRefuse(DemandeAccesEntity demande, String motif) {
        UtilisateursEntity parent = demande.getUtilisateur();
        ClassesEntity classe = demande.getClasse();
        String enfantNom = nomEnfant(demande.getEleveAssocieId());
        String message = "La demande d'inscription de votre enfant " + enfantNom + " à la classe " + classe.getNom()
                + " a été refusée" + (motif != null && !motif.isBlank() ? ". Motif : " + motif.trim() : ".");
        notifier(parent.getId(), TYPE_CHILD_ACCESS_REJECTED, "Demande pour votre enfant refusée", message,
                demande.getEleveAssocieId(), enfantNom, classe.getId());
        if (parent.getEmail() != null) {
            String email = parent.getEmail();
            String nomParent = nomComplet(parent);
            String classeNom = classe.getNom();
            InscriptionClasseService.apresCommit(() -> emailService.envoyerEnfantRefuse(email, nomParent, enfantNom, classeNom, motif));
        }
    }

    private void notifier(String userId, String type, String titre, String message, String enfantId, String enfantNom,
                          String classeId) {
        try {
            notificationService.createNotification(userId, type, titre, message, enfantId, enfantNom, classeId, "CLASS");
        } catch (Exception e) {
            log.error("Child access decision notification could not be created for {}: {}", userId, e.getMessage());
        }
    }

    public String nomEnfant(String eleveId) {
        return daoAccessorService.getRepository(UtilisateursRepository.class).findById(eleveId)
                .map(InscriptionParentService::nomComplet).orElse("votre enfant");
    }

    private List<ElevesEntity> enfantsDe(String parentId) {
        List<String> ids = daoAccessorService.getRepository(ParentEleveRepository.class).findEleveIdsByParentId(parentId);
        if (ids.isEmpty()) return Collections.emptyList();
        List<ElevesEntity> enfants = new ArrayList<>(daoAccessorService.getRepository(ElevesRepository.class).findAllById(ids));
        enfants.sort(Comparator.comparing((ElevesEntity e) -> e.getCreationDate(), Comparator.nullsLast(Comparator.naturalOrder())));
        return enfants;
    }

    static String nomComplet(UtilisateursEntity u) {
        String p = u.getPrenom() == null ? "" : u.getPrenom();
        String n = u.getNom() == null ? "" : u.getNom();
        return (p + " " + n).trim();
    }
}
