package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.ContratEmailService;
import cmr.notep.business.services.PaymentService;
import cmr.notep.interfaces.dto.ContratActionDto;
import cmr.notep.interfaces.dto.PaymentInfoDto;
import cmr.notep.interfaces.dto.RenouvellementInfoRequestDto;
import cmr.notep.interfaces.dto.RenouvellementStatutDto;
import cmr.notep.interfaces.modeles.Contrat;
import cmr.notep.interfaces.modeles.Offre;
import cmr.notep.modele.EtatClasse;
import cmr.notep.modele.PeriodiciteContrat;
import cmr.notep.modele.StatutContrat;
import cmr.notep.modele.TypeCibleOffre;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class ContratBusiness {

    public static final String MOTIF_EXPIRATION = "Expiration du contrat";

    private final DaoAccessorService daoAccessorService;
    private final PaymentService paymentService;
    private final ContratEmailService contratEmailService;
    private final OffreBusiness offreBusiness;
    private final cmr.notep.business.utils.JwtUtil jwtUtil;

    /**
     * Resout, pour un utilisateur donne (professeur moderateur/membre d'une classe, ou gestionnaire
     * d'etablissement), la liste des classes/etablissements dont l'offre est expiree. Utilise au
     * moment de la connexion (voir AuthBusiness.loginUser) pour avertir sans bloquer l'authentification.
     */
    @lombok.Value
    public static class AccesUtilisateurInfo {
        List<Map<String, String>> expiredEntities;
        boolean hasActiveEntity;
    }

    /**
     * Resout, pour un utilisateur donne (professeur moderateur/membre de classe(s), ou gestionnaire
     * d'etablissement(s)), la liste de ses classes/etablissements dont l'offre est expiree, ET si au
     * moins une de ses classes/etablissements reste active. Un utilisateur peut avoir plusieurs
     * classes : tant qu'il lui en reste au moins une utilisable, on ne bloque jamais la connexion
     * (voir AuthBusiness.loginUser) - seule l'entite concernee est signalee comme expiree.
     */
    public AccesUtilisateurInfo resoudreAccesUtilisateur(String utilisateurId) {
        List<Map<String, String>> expired = new java.util.ArrayList<>();
        if (utilisateurId == null) return new AccesUtilisateurInfo(expired, true);
        LocalDateTime now = LocalDateTime.now();
        boolean[] hasActive = {false};

        // Seuls les professeurs (moderateur ou simple membre via droit d'acces) sont concernes par le
        // verrouillage de connexion lie a l'offre. Un parent/eleve ayant uniquement un droit d'acces
        // (AccederEntity) a une classe ne doit jamais etre bloque a la connexion pour une offre qu'il
        // ne gere pas - AccederEntity est generique (parents, eleves, professeurs invites...).
        Set<String> classeIds = new LinkedHashSet<>();
        boolean isProfessor = daoAccessorService.getRepository(ProfesseursRepository.class).findById(utilisateurId)
                .map(prof -> {
                    prof.getModeratedClasses().forEach(c -> classeIds.add(c.getId()));
                    return true;
                })
                .orElse(false);
        if (isProfessor) {
            daoAccessorService.getRepository(AccederRepository.class).findByUtilisateurId(utilisateurId)
                    .forEach(a -> classeIds.add(a.getClasseId()));
        }

        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        for (String classeId : classeIds) {
            classesRepository.findById(classeId).ifPresent(classe -> {
                // Une classe rattachee a un etablissement herite du contrat de l'etablissement.
                ContratEntity contrat = classe.getEtablissement() != null
                        ? trouverDernierContrat(null, classe.getEtablissement().getId())
                        : trouverDernierContrat(classeId, null);
                if (contrat == null) {
                    hasActive[0] = true; // pas d'offre associee = pas de restriction sur cette classe
                } else if (estExpire(contrat, now)) {
                    expired.add(entiteExpiree("CLASSE", classe.getId(), classe.getNom()));
                } else {
                    hasActive[0] = true;
                }
            });
        }

        List<EtablissementEntity> etablissementsGeres =
                daoAccessorService.getRepository(EtablissementRepository.class).findByGestionnaireId(utilisateurId);
        for (EtablissementEntity etab : etablissementsGeres) {
            ContratEntity contrat = trouverDernierContrat(null, etab.getId());
            if (contrat == null) {
                hasActive[0] = true;
            } else if (estExpire(contrat, now)) {
                expired.add(entiteExpiree("ETABLISSEMENT", etab.getId(), etab.getNom()));
            } else {
                hasActive[0] = true;
            }
        }

        // Utilisateur non rattache a une classe/etablissement a offre (eleve, parent, admin...) :
        // aucune restriction ne s'applique, on ne bloque jamais.
        if (classeIds.isEmpty() && etablissementsGeres.isEmpty()) {
            hasActive[0] = true;
        }

        return new AccesUtilisateurInfo(expired, hasActive[0]);
    }

    /**
     * Verrou applicatif : interdit toute modification d'une classe dont l'offre (propre ou, si elle
     * appartient a un etablissement, celle de l'etablissement) est expiree. "La classe sera plus
     * modifiable a partir de telle date" -> l'acces en lecture reste possible, seule l'ecriture est bloquee.
     */
    public void verifierAbonnementActif(String classeId) {
        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class).findById(classeId).orElse(null);
        if (classe == null) return;
        LocalDateTime now = LocalDateTime.now();
        ContratEntity contrat = classe.getEtablissement() != null
                ? trouverDernierContrat(null, classe.getEtablissement().getId())
                : trouverDernierContrat(classeId, null);
        if (estExpire(contrat, now)) {
            throw new SchoolException(SchoolErrorCode.ABONNEMENT_EXPIRE,
                    "Votre offre a expiré, veuillez la renouveler pour continuer à modifier cette classe");
        }
    }

    private boolean estExpire(ContratEntity contrat, LocalDateTime now) {
        if (contrat == null) return false;
        if (contrat.getStatut() == StatutContrat.EXPIRE) return true;
        return contrat.getStatut() == StatutContrat.ACTIF && contrat.getDateFin() != null && contrat.getDateFin().isBefore(now);
    }

    private Map<String, String> entiteExpiree(String type, String id, String nom) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("id", id);
        m.put("nom", nom);
        return m;
    }

    public String resolveUserIdByEmail(String email) {
        if (email == null) return null;
        return daoAccessorService.getRepository(UtilisateursRepository.class)
                .findByEmail(email)
                .map(UtilisateursEntity::getId)
                .orElse(null);
    }

    // ---------------------------------------------------------------------
    // Lecture
    // ---------------------------------------------------------------------

    public Contrat obtenirContratCourantDeLaClasse(String classeId) {
        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class).findById(classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable: " + classeId));
        if (classe.getEtablissement() != null) {
            return obtenirContratCourantDeLetablissement(classe.getEtablissement().getId());
        }
        ContratEntity entity = trouverDernierContrat(classeId, null);
        if (entity == null) {
            throw new SchoolException(SchoolErrorCode.CONTRAT_INTROUVABLE, "Aucun contrat pour cette classe");
        }
        return toDto(entity, null, null);
    }

    public Contrat obtenirContratCourantDeLetablissement(String etablissementId) {
        ContratEntity entity = trouverDernierContrat(null, etablissementId);
        if (entity == null) {
            throw new SchoolException(SchoolErrorCode.CONTRAT_INTROUVABLE, "Aucun contrat pour cet établissement");
        }
        Integer max = null;
        if (entity.getStatut() == StatutContrat.ACTIF && entity.getOffre().getNombreClassesInclues() != null) {
            int bonus = entity.getOffre().getClassesBonus() != null ? entity.getOffre().getClassesBonus() : 0;
            max = entity.getOffre().getNombreClassesInclues() + bonus;
        }
        long utilisees = daoAccessorService.getRepository(ClassesRepository.class)
                .countByEtablissementIdAndEtat(etablissementId, EtatClasse.ACTIF);
        return toDto(entity, (int) utilisees, max);
    }

    private ContratEntity trouverDernierContrat(String classeId, String etablissementId) {
        ContratRepository repo = daoAccessorService.getRepository(ContratRepository.class);
        List<ContratEntity> tous = classeId != null
                ? repo.findByClasseIdOrderByDateCreationDesc(classeId)
                : repo.findByEtablissementIdOrderByDateCreationDesc(etablissementId);
        return tous.isEmpty() ? null : tous.get(0);
    }

    // ---------------------------------------------------------------------
    // Souscription automatique (appelee depuis ClassesBusiness / EtablissementBusiness)
    // ---------------------------------------------------------------------

    public ContratEntity souscrireEtActiverPourClasse(String offreId, PeriodiciteContrat periodicite, ClassesEntity classe, String createdBy) {
        return souscrireEtActiver(offreId, periodicite, classe, null, createdBy);
    }

    public ContratEntity souscrireEtActiverPourEtablissement(String offreId, PeriodiciteContrat periodicite, EtablissementEntity etablissement, String createdBy) {
        return souscrireEtActiver(offreId, periodicite, null, etablissement, createdBy);
    }

    private ContratEntity souscrireEtActiver(String offreId, PeriodiciteContrat periodicite, ClassesEntity classe, EtablissementEntity etablissement, String createdBy) {
        if (offreId == null || offreId.trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Une offre doit être sélectionnée");
        }
        if (periodicite == null) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "La périodicité (mensuel/annuel) doit être précisée");
        }
        OffreEntity offre = offreBusiness.obtenirEntiteParId(offreId);
        TypeCibleOffre attendu = classe != null ? TypeCibleOffre.CLASSE : TypeCibleOffre.ETABLISSEMENT;
        if (offre.getCible() != attendu) {
            throw new SchoolException(SchoolErrorCode.CIBLE_OFFRE_INVALIDE,
                    "Cette offre ne correspond pas au type demandé (" + attendu + ")");
        }

        BigDecimal prix;
        Long dureeMinutes;
        if (periodicite == PeriodiciteContrat.ANNUEL) {
            prix = offre.getPrixAnnuel();
            dureeMinutes = offre.getDureeAnnuelleMinutes();
        } else {
            prix = offre.getPrixMensuel();
            dureeMinutes = offre.getDureeMensuelleMinutes();
        }
        if (prix == null || dureeMinutes == null) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Cette offre ne propose pas la périodicité " + periodicite);
        }

        ContratEntity entity = new ContratEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setOffre(offre);
        entity.setClasse(classe);
        entity.setEtablissement(etablissement);
        entity.setPeriodicite(periodicite);
        entity.setPrixPaye(prix);
        LocalDateTime now = LocalDateTime.now();
        entity.setDateDebut(now);
        entity.setDateFin(now.plusMinutes(dureeMinutes));
        entity.setStatut(StatutContrat.ACTIF);
        entity.setDatePaiement(now);
        entity.setCreatedBy(createdBy);
        entity.setDateCreation(now);

        ContratEntity saved = daoAccessorService.getRepository(ContratRepository.class).save(entity);
        contratEmailService.sendConfirmationSouscriptionEmail(saved);
        return saved;
    }

    // ---------------------------------------------------------------------
    // Attribution directe par un admin (support), sans paiement
    // ---------------------------------------------------------------------

    /**
     * Attribue directement une offre a une classe ou un etablissement (exactement l'un des deux id
     * doit etre fourni), sans passer par la simulation de paiement. Reserve a l'admin (voir
     * @PreAuthorize sur ContratService). Utile en support client : remplacement d'un moyen de
     * paiement en panne, geste commercial, correction d'une erreur, etc.
     */
    public Contrat assignerOffreParAdmin(String classeId, String etablissementId, String offreId,
                                          PeriodiciteContrat periodicite, String adminUserId) {
        if (offreId == null || offreId.trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Une offre doit être sélectionnée");
        }
        if (periodicite == null) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "La périodicité (mensuel/annuel) doit être précisée");
        }
        OffreEntity offre = offreBusiness.obtenirEntiteParId(offreId);
        TypeCibleOffre attendu = classeId != null ? TypeCibleOffre.CLASSE : TypeCibleOffre.ETABLISSEMENT;
        if (offre.getCible() != attendu) {
            throw new SchoolException(SchoolErrorCode.CIBLE_OFFRE_INVALIDE, "Cette offre ne correspond pas au type demandé");
        }
        BigDecimal prix = periodicite == PeriodiciteContrat.ANNUEL ? offre.getPrixAnnuel() : offre.getPrixMensuel();
        Long dureeMinutes = periodicite == PeriodiciteContrat.ANNUEL ? offre.getDureeAnnuelleMinutes() : offre.getDureeMensuelleMinutes();
        if (prix == null || dureeMinutes == null) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Cette offre ne propose pas la périodicité " + periodicite);
        }

        ContratRepository contratRepository = daoAccessorService.getRepository(ContratRepository.class);
        ContratEntity actuel = trouverDernierContrat(classeId, etablissementId);
        if (actuel != null && actuel.getStatut() != StatutContrat.RESILIE && actuel.getStatut() != StatutContrat.EXPIRE) {
            actuel.setStatut(StatutContrat.RESILIE);
            contratRepository.save(actuel);
        }

        ContratEntity nouveau = new ContratEntity();
        nouveau.setId(UUID.randomUUID().toString());
        nouveau.setOffre(offre);
        LocalDateTime now = LocalDateTime.now();
        if (classeId != null) {
            nouveau.setClasse(getClasse(classeId));
        } else {
            nouveau.setEtablissement(getEtablissement(etablissementId));
        }
        nouveau.setPeriodicite(periodicite);
        nouveau.setPrixPaye(prix);
        nouveau.setDateDebut(now);
        nouveau.setDateFin(now.plusMinutes(dureeMinutes));
        nouveau.setStatut(StatutContrat.ACTIF);
        nouveau.setDatePaiement(now);
        nouveau.setContratPrecedent(actuel);
        nouveau.setCreatedBy(adminUserId);
        nouveau.setDateCreation(now);
        nouveau.setAccordeParAdmin(true);
        ContratEntity saved = contratRepository.save(nouveau);

        if (nouveau.getClasse() != null) {
            reactiverClasseSiNecessaire(nouveau.getClasse(), adminUserId);
        } else {
            EtablissementEntity etab = nouveau.getEtablissement();
            etab.setExpireParOffre(false);
            daoAccessorService.getRepository(EtablissementRepository.class).save(etab);
            daoAccessorService.getRepository(ClassesRepository.class)
                    .findByEtablissementIdAndEtat(etab.getId(), EtatClasse.INACTIF)
                    .forEach(classe -> reactiverClasseSiExpiration(classe, adminUserId));
        }

        contratEmailService.sendConfirmationSouscriptionEmail(saved);
        log.info("Offre {} attribuée par l'admin {} ({})", offreId, adminUserId, classeId != null ? "classe " + classeId : "établissement " + etablissementId);
        return toDto(saved, null, null);
    }

    // ---------------------------------------------------------------------
    // Quota etablissement
    // ---------------------------------------------------------------------

    public void verifierQuotaEtablissement(String etablissementId) {
        ContratEntity contrat = trouverDernierContrat(null, etablissementId);
        if (contrat == null || contrat.getStatut() != StatutContrat.ACTIF) {
            // Pas de forfait actif associe : pas de restriction imposee par ce module.
            return;
        }
        OffreEntity offre = contrat.getOffre();
        if (offre.getNombreClassesInclues() == null) {
            return; // offre sans limite explicite
        }
        int max = offre.getNombreClassesInclues() + (offre.getClassesBonus() != null ? offre.getClassesBonus() : 0);
        long actuelles = daoAccessorService.getRepository(ClassesRepository.class)
                .countByEtablissementIdAndEtat(etablissementId, EtatClasse.ACTIF);
        if (actuelles >= max) {
            throw new SchoolException(SchoolErrorCode.QUOTA_CLASSES_ATTEINT,
                    "Le forfait de votre établissement autorise au maximum " + max +
                            " classe(s) actives. Renouvelez ou changez d'offre pour en créer davantage.");
        }
    }

    // ---------------------------------------------------------------------
    // Prolonger / changer d'offre (classe ou etablissement)
    // ---------------------------------------------------------------------

    public Contrat prolongerContratClasse(String classeId, ContratActionDto action, String callerUserId, boolean callerIsAdmin) {
        ClassesEntity classe = getClasse(classeId);
        verifierAccesClasse(classe, callerUserId, callerIsAdmin);
        ContratEntity actuel = exigerDernierContrat(classeId, null);
        return toDto(appliquerRenouvellement(actuel, actuel.getOffre().getId(), action, callerUserId), null, null);
    }

    public Contrat changerOffreClasse(String classeId, ContratActionDto action, String callerUserId, boolean callerIsAdmin) {
        ClassesEntity classe = getClasse(classeId);
        verifierAccesClasse(classe, callerUserId, callerIsAdmin);
        ContratEntity actuel = exigerDernierContrat(classeId, null);
        if (action.getNouvelleOffreId() == null || action.getNouvelleOffreId().trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "La nouvelle offre doit être précisée");
        }
        return toDto(appliquerRenouvellement(actuel, action.getNouvelleOffreId(), action, callerUserId), null, null);
    }

    public Contrat prolongerContratEtablissement(String etablissementId, ContratActionDto action, String callerUserId, boolean callerIsAdmin) {
        EtablissementEntity etablissement = getEtablissement(etablissementId);
        verifierAccesEtablissement(etablissement, callerUserId, callerIsAdmin);
        ContratEntity actuel = exigerDernierContrat(null, etablissementId);
        ContratEntity nouveau = appliquerRenouvellement(actuel, actuel.getOffre().getId(), action, callerUserId);
        return toDtoEtablissement(nouveau, etablissementId);
    }

    public Contrat changerOffreEtablissement(String etablissementId, ContratActionDto action, String callerUserId, boolean callerIsAdmin) {
        EtablissementEntity etablissement = getEtablissement(etablissementId);
        verifierAccesEtablissement(etablissement, callerUserId, callerIsAdmin);
        ContratEntity actuel = exigerDernierContrat(null, etablissementId);
        if (action.getNouvelleOffreId() == null || action.getNouvelleOffreId().trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "La nouvelle offre doit être précisée");
        }
        OffreEntity nouvelleOffre = offreBusiness.obtenirEntiteParId(action.getNouvelleOffreId());
        if (nouvelleOffre.getNombreClassesInclues() != null) {
            int nouveauMax = nouvelleOffre.getNombreClassesInclues() +
                    (nouvelleOffre.getClassesBonus() != null ? nouvelleOffre.getClassesBonus() : 0);
            long actuelles = daoAccessorService.getRepository(ClassesRepository.class)
                    .countByEtablissementIdAndEtat(etablissementId, EtatClasse.ACTIF);
            if (actuelles > nouveauMax) {
                throw new SchoolException(SchoolErrorCode.QUOTA_CLASSES_ATTEINT,
                        "Cette offre n'autorise que " + nouveauMax + " classe(s), or " + actuelles +
                                " classe(s) sont déjà actives dans cet établissement.");
            }
        }
        ContratEntity nouveau = appliquerRenouvellement(actuel, action.getNouvelleOffreId(), action, callerUserId);
        return toDtoEtablissement(nouveau, etablissementId);
    }

    private ContratEntity appliquerRenouvellement(ContratEntity actuel, String offreId, ContratActionDto action, String callerUserId) {
        PeriodiciteContrat periodicite = action.getPeriodicite() != null ? action.getPeriodicite() : actuel.getPeriodicite();
        OffreEntity offre = offreBusiness.obtenirEntiteParId(offreId);
        TypeCibleOffre attendu = actuel.getClasse() != null ? TypeCibleOffre.CLASSE : TypeCibleOffre.ETABLISSEMENT;
        if (offre.getCible() != attendu) {
            throw new SchoolException(SchoolErrorCode.CIBLE_OFFRE_INVALIDE, "Cette offre ne correspond pas à la cible du contrat");
        }

        BigDecimal prix = periodicite == PeriodiciteContrat.ANNUEL ? offre.getPrixAnnuel() : offre.getPrixMensuel();
        Long dureeMinutes = periodicite == PeriodiciteContrat.ANNUEL ? offre.getDureeAnnuelleMinutes() : offre.getDureeMensuelleMinutes();
        if (prix == null || dureeMinutes == null) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Cette offre ne propose pas la périodicité " + periodicite);
        }

        boolean paiementOk = paymentService.processPayment(toPaymentInfoDtoLegacy(action.getPaymentInfo()));
        if (!paiementOk) {
            throw new SchoolException(SchoolErrorCode.PAYMENT_FAILED, "Échec du paiement, réessayez");
        }

        ContratEntity nouveau = new ContratEntity();
        nouveau.setId(UUID.randomUUID().toString());
        nouveau.setOffre(offre);
        nouveau.setClasse(actuel.getClasse());
        nouveau.setEtablissement(actuel.getEtablissement());
        nouveau.setPeriodicite(periodicite);
        nouveau.setPrixPaye(prix);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = actuel.getStatut() == StatutContrat.ACTIF && actuel.getDateFin() != null && actuel.getDateFin().isAfter(now)
                ? actuel.getDateFin() : now;
        nouveau.setDateDebut(now);
        nouveau.setDateFin(base.plusMinutes(dureeMinutes));
        nouveau.setStatut(StatutContrat.ACTIF);
        nouveau.setDatePaiement(now);
        nouveau.setContratPrecedent(actuel);
        nouveau.setCreatedBy(callerUserId);
        nouveau.setDateCreation(now);

        ContratRepository contratRepository = daoAccessorService.getRepository(ContratRepository.class);
        if (actuel.getStatut() != StatutContrat.RESILIE && actuel.getStatut() != StatutContrat.EXPIRE) {
            actuel.setStatut(StatutContrat.RESILIE);
            contratRepository.save(actuel);
        }
        ContratEntity saved = contratRepository.save(nouveau);

        if (nouveau.getClasse() != null) {
            reactiverClasseSiNecessaire(nouveau.getClasse(), callerUserId);
        } else if (nouveau.getEtablissement() != null) {
            EtablissementEntity etablissement = nouveau.getEtablissement();
            etablissement.setExpireParOffre(false);
            daoAccessorService.getRepository(EtablissementRepository.class).save(etablissement);
            daoAccessorService.getRepository(ClassesRepository.class)
                    .findByEtablissementIdAndEtat(etablissement.getId(), EtatClasse.INACTIF)
                    .forEach(classe -> reactiverClasseSiExpiration(classe, callerUserId));
        }

        contratEmailService.sendConfirmationSouscriptionEmail(saved);
        return saved;
    }

    private ContratEntity exigerDernierContrat(String classeId, String etablissementId) {
        ContratEntity contrat = trouverDernierContrat(classeId, etablissementId);
        if (contrat == null) {
            throw new SchoolException(SchoolErrorCode.CONTRAT_INTROUVABLE, "Aucun contrat existant à renouveler");
        }
        return contrat;
    }

    // ---------------------------------------------------------------------
    // Expiration / (re)activation
    // ---------------------------------------------------------------------

    public void desactiverClassePourExpiration(ClassesEntity classe) {
        if (classe.getEtat() == EtatClasse.INACTIF) {
            return;
        }
        classe.setEtat(EtatClasse.INACTIF);
        classe.setExpireParOffre(true);
        daoAccessorService.getRepository(ClassesRepository.class).save(classe);
        creerHistoActivation(classe, false, MOTIF_EXPIRATION, EtatClasse.INACTIF);
    }

    private void reactiverClasseSiNecessaire(ClassesEntity classe, String utilisateurId) {
        classe.setEtat(EtatClasse.ACTIF);
        classe.setExpireParOffre(false);
        daoAccessorService.getRepository(ClassesRepository.class).save(classe);
        creerHistoActivation(classe, true, "Réactivation après renouvellement de l'offre", EtatClasse.ACTIF);
    }

    private void reactiverClasseSiExpiration(ClassesEntity classe, String utilisateurId) {
        // Ne reactive automatiquement (cascade etablissement) que les classes desactivees pour expiration,
        // pas celles rejetees par l'etablissement pour une autre raison.
        List<HistoActivationEntity> historique = daoAccessorService.getRepository(HistoActivationRepository.class)
                .findByClasseId(classe.getId());
        boolean desactiveePourExpiration = historique.stream()
                .filter(h -> !h.isActive())
                .reduce((first, second) -> second) // dernier element
                .map(h -> MOTIF_EXPIRATION.equals(h.getMotifDesactivation()))
                .orElse(false);
        if (desactiveePourExpiration) {
            reactiverClasseSiNecessaire(classe, utilisateurId);
        }
    }

    private void creerHistoActivation(ClassesEntity classe, boolean active, String motif, EtatClasse etat) {
        HistoActivationEntity histo = new HistoActivationEntity();
        histo.setId(UUID.randomUUID().toString());
        histo.setClasse(classe);
        UtilisateursEntity utilisateur = classe.getModerator() != null
                ? daoAccessorService.getRepository(UtilisateursRepository.class).findById(classe.getModerator().getId()).orElse(null)
                : null;
        histo.setUtilisateur(utilisateur);
        histo.setDateActivation(active ? LocalDateTime.now() : (classe.getDateCreation() != null
                ? classe.getDateCreation().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : LocalDateTime.now()));
        if (!active) {
            histo.setDateDesactivation(LocalDateTime.now());
        }
        histo.setMotifDesactivation(active ? null : motif);
        histo.setActive(active);
        histo.setEtatClasse(etat);
        if (histo.getUtilisateur() != null) {
            daoAccessorService.getRepository(HistoActivationRepository.class).save(histo);
        }
    }

    // ---------------------------------------------------------------------
    // Renouvellement sans session (via lien email)
    // ---------------------------------------------------------------------

    public void demanderLienRenouvellement(RenouvellementInfoRequestDto request) {
        String entityType;
        String entityId;
        String emailCible = null;
        String nomCible = null;

        if (request.getClasseId() != null && !request.getClasseId().trim().isEmpty()) {
            ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                    .findById(request.getClasseId()).orElse(null);
            if (classe != null && classe.getModerator() != null) {
                emailCible = classe.getModerator().getEmail();
                nomCible = classe.getNom();
            }
            entityType = "CLASSE";
            entityId = request.getClasseId();
        } else if (request.getEtablissementId() != null && !request.getEtablissementId().trim().isEmpty()) {
            EtablissementEntity etablissement = daoAccessorService.getRepository(EtablissementRepository.class)
                    .findById(request.getEtablissementId()).orElse(null);
            if (etablissement != null && etablissement.getGestionnaire() != null) {
                emailCible = etablissement.getGestionnaire().getEmail();
                nomCible = etablissement.getNom();
            }
            entityType = "ETABLISSEMENT";
            entityId = request.getEtablissementId();
        } else {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Identifiant de classe ou d'établissement requis");
        }

        // Pas d'enumeration : on ne revele jamais si l'email/l'id correspondent ou non.
        if (emailCible == null || request.getEmail() == null || !emailCible.equalsIgnoreCase(request.getEmail().trim())) {
            log.info("Demande de renouvellement non concluante pour {} {}", entityType, entityId);
            return;
        }

        ContratEntity contrat = trouverDernierContrat(
                "CLASSE".equals(entityType) ? entityId : null,
                "ETABLISSEMENT".equals(entityType) ? entityId : null);
        String token = jwtUtil.generateRenewalToken(entityType, entityId);
        if (contrat != null) {
            contrat.setRenewalToken(token);
            daoAccessorService.getRepository(ContratRepository.class).save(contrat);
        }
        contratEmailService.sendRenouvellementLienEmail(emailCible, nomCible, entityType, entityId, token);
    }

    public RenouvellementStatutDto obtenirStatutRenouvellement(String token) {
        String entityType = validerEtLireToken(token);
        String entityId = jwtUtil.getEntityIdFromRenewalToken(token);

        if ("CLASSE".equals(entityType)) {
            ClassesEntity classe = getClasse(entityId);
            ContratEntity contrat = trouverDernierContrat(entityId, null);
            return RenouvellementStatutDto.builder()
                    .classeId(entityId)
                    .nom(classe.getNom())
                    .contratCourant(contrat != null ? toDto(contrat, null, null) : null)
                    .offresDisponibles(offreBusiness.listerOffres(TypeCibleOffre.CLASSE, false))
                    .build();
        } else {
            EtablissementEntity etablissement = getEtablissement(entityId);
            ContratEntity contrat = trouverDernierContrat(null, entityId);
            return RenouvellementStatutDto.builder()
                    .etablissementId(entityId)
                    .nom(etablissement.getNom())
                    .contratCourant(contrat != null ? toDto(contrat, null, null) : null)
                    .offresDisponibles(offreBusiness.listerOffres(TypeCibleOffre.ETABLISSEMENT, false))
                    .build();
        }
    }

    public Contrat prolongerParToken(String token, ContratActionDto action) {
        String entityType = validerEtLireToken(token);
        String entityId = jwtUtil.getEntityIdFromRenewalToken(token);
        if ("CLASSE".equals(entityType)) {
            ContratEntity actuel = exigerDernierContrat(entityId, null);
            return toDto(appliquerRenouvellement(actuel, actuel.getOffre().getId(), action, "renouvellement-token"), null, null);
        }
        ContratEntity actuel = exigerDernierContrat(null, entityId);
        ContratEntity nouveau = appliquerRenouvellement(actuel, actuel.getOffre().getId(), action, "renouvellement-token");
        return toDtoEtablissement(nouveau, entityId);
    }

    public Contrat changerOffreParToken(String token, ContratActionDto action) {
        String entityType = validerEtLireToken(token);
        String entityId = jwtUtil.getEntityIdFromRenewalToken(token);
        if (action.getNouvelleOffreId() == null || action.getNouvelleOffreId().trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "La nouvelle offre doit être précisée");
        }
        if ("CLASSE".equals(entityType)) {
            ContratEntity actuel = exigerDernierContrat(entityId, null);
            return toDto(appliquerRenouvellement(actuel, action.getNouvelleOffreId(), action, "renouvellement-token"), null, null);
        }
        ContratEntity actuel = exigerDernierContrat(null, entityId);
        ContratEntity nouveau = appliquerRenouvellement(actuel, action.getNouvelleOffreId(), action, "renouvellement-token");
        return toDtoEtablissement(nouveau, entityId);
    }

    private String validerEtLireToken(String token) {
        if (!jwtUtil.validateRenewalToken(token)) {
            throw new SchoolException(SchoolErrorCode.RENOUVELLEMENT_TOKEN_INVALIDE, "Lien de renouvellement invalide ou expiré");
        }
        return jwtUtil.getEntityTypeFromRenewalToken(token);
    }

    // ---------------------------------------------------------------------
    // Controle d'acces
    // ---------------------------------------------------------------------

    private void verifierAccesClasse(ClassesEntity classe, String callerUserId, boolean callerIsAdmin) {
        if (callerIsAdmin) return;
        if (callerUserId == null) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, "Authentification requise");
        }
        boolean estModerateur = classe.getModerator() != null && classe.getModerator().getId().equals(callerUserId);
        boolean aAcces = daoAccessorService.getRepository(AccederRepository.class)
                .existsByUtilisateurIdAndClasseId(callerUserId, classe.getId());
        if (!estModerateur && !aAcces) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED,
                    "Seuls les enseignants de cette classe peuvent gérer son offre");
        }
    }

    private void verifierAccesEtablissement(EtablissementEntity etablissement, String callerUserId, boolean callerIsAdmin) {
        if (callerIsAdmin) return;
        if (callerUserId == null) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, "Authentification requise");
        }
        boolean estGestionnaire = etablissement.getGestionnaire() != null && etablissement.getGestionnaire().getId().equals(callerUserId);
        if (!estGestionnaire) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED,
                    "Seul le gestionnaire de cet établissement peut gérer son offre");
        }
    }

    // ---------------------------------------------------------------------
    // Utilitaires
    // ---------------------------------------------------------------------

    private ClassesEntity getClasse(String classeId) {
        return daoAccessorService.getRepository(ClassesRepository.class).findById(classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable: " + classeId));
    }

    private EtablissementEntity getEtablissement(String etablissementId) {
        return daoAccessorService.getRepository(EtablissementRepository.class).findById(etablissementId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement introuvable: " + etablissementId));
    }

    private cmr.notep.interfaces.dto.ClasseCreationDto.PaymentInfoDto toPaymentInfoDtoLegacy(PaymentInfoDto dto) {
        if (dto == null) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Informations de paiement requises");
        }
        return cmr.notep.interfaces.dto.ClasseCreationDto.PaymentInfoDto.builder()
                .paymentMethod(dto.getPaymentMethod())
                .cardNumber(dto.getCardNumber())
                .expiryDate(dto.getExpiryDate())
                .cvv(dto.getCvv())
                .cardHolderName(dto.getCardHolderName())
                .phoneNumber(dto.getPhoneNumber())
                .amount(dto.getAmount())
                .build();
    }

    private Contrat toDto(ContratEntity entity, Integer classesUtilisees, Integer classesMax) {
        OffreEntity offre = entity.getOffre();

        boolean suppressionImminente = false;
        LocalDateTime dateSuppressionPrevue = null;
        if (entity.getStatut() == StatutContrat.EXPIRE && offre.getDelaiSuppressionMinutes() != null) {
            LocalDateTime pointDepart = entity.getDateFin() != null ? entity.getDateFin() : entity.getDateCreation();
            if (pointDepart != null) {
                dateSuppressionPrevue = pointDepart.plusMinutes(offre.getDelaiSuppressionMinutes());
                LocalDateTime seuilAvertissement = offre.getDelaiRappelSuppressionMinutes() != null
                        ? pointDepart.plusMinutes(offre.getDelaiRappelSuppressionMinutes())
                        : dateSuppressionPrevue;
                suppressionImminente = !LocalDateTime.now().isBefore(seuilAvertissement);
            }
        }

        return Contrat.builder()
                .id(entity.getId())
                .offreId(offre.getId())
                .offreNom(offre.getNom())
                .classeId(entity.getClasse() != null ? entity.getClasse().getId() : null)
                .etablissementId(entity.getEtablissement() != null ? entity.getEtablissement().getId() : null)
                .periodicite(entity.getPeriodicite())
                .prixPaye(entity.getPrixPaye())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .statut(entity.getStatut())
                .datePaiement(entity.getDatePaiement())
                .contratPrecedentId(entity.getContratPrecedent() != null ? entity.getContratPrecedent().getId() : null)
                .createdBy(entity.getCreatedBy())
                .dateCreation(entity.getDateCreation())
                .classesUtilisees(classesUtilisees)
                .classesMax(classesMax)
                .prixMensuel(offre.getPrixMensuel())
                .prixAnnuel(offre.getPrixAnnuel())
                .elevesMax(offre.getElevesMax())
                .stockageMax(offre.getStockageMaxGo())
                .messagerie(offre.getMessagerieIncluse())
                .suppressionImminente(suppressionImminente)
                .dateSuppressionPrevue(dateSuppressionPrevue)
                .build();
    }

    private Contrat toDtoEtablissement(ContratEntity entity, String etablissementId) {
        Integer max = null;
        if (entity.getStatut() == StatutContrat.ACTIF && entity.getOffre().getNombreClassesInclues() != null) {
            int bonus = entity.getOffre().getClassesBonus() != null ? entity.getOffre().getClassesBonus() : 0;
            max = entity.getOffre().getNombreClassesInclues() + bonus;
        }
        long utilisees = daoAccessorService.getRepository(ClassesRepository.class)
                .countByEtablissementIdAndEtat(etablissementId, EtatClasse.ACTIF);
        return toDto(entity, (int) utilisees, max);
    }
}
