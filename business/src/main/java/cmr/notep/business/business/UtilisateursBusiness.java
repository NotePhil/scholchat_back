package cmr.notep.business.business;

import cmr.notep.business.security.UserSubtypeService;
import cmr.notep.business.services.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.modele.StatutVerificationProfesseur;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.transaction.annotation.Transactional;

import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.interfaces.modeles.Utilisateurs;

import cmr.notep.ressourcesjpa.dao.MotifRejetEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.MotifRejetRepository;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import cmr.notep.ressourcesjpa.repository.ParentsRepository;
import cmr.notep.ressourcesjpa.repository.ElevesRepository;
import cmr.notep.ressourcesjpa.repository.UserRoleRepository;
import cmr.notep.ressourcesjpa.repository.ParentEleveRepository;
import java.util.Optional;


import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;
@Component
@Slf4j
@Transactional(noRollbackFor = SchoolException.class)
public class UtilisateursBusiness {
    private final UserSubtypeService userSubtypeService;
    private final DaoAccessorService daoAccessorService ;
    private final ActivationEmailService activationEmailService;
    private final JwtUtil jwtUtil;
    private final MailServiceInterface mailService;
    private final IRejectionEmailService rejectionEmailService;
    private final RoleService roleService;
    private final UserValidationService userValidationService;
    private final AwaitingValidationEmailService awaitingValidationEmailService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private NotificationService notificationService;

    @Autowired
    private cmr.notep.business.security.ProfesseurVerificationService professeurVerification;

    public UtilisateursBusiness(DaoAccessorService daoAccessorService,
                                ActivationEmailService activationEmailService,
                                JwtUtil jwtUtil,
                                MailServiceInterface mailService,
                                IRejectionEmailService rejectionEmailService,
                                RoleService roleService, UserValidationService userValidationService, UserValidationService userValidationService1,
                                AwaitingValidationEmailService awaitingValidationEmailService,
            UserSubtypeService userSubtypeService) {
        this.userSubtypeService = userSubtypeService;
        this.daoAccessorService = daoAccessorService;
        this.activationEmailService = activationEmailService;
        this.jwtUtil = jwtUtil;
        this.mailService = mailService;
        this.rejectionEmailService = rejectionEmailService;
        this.roleService = roleService;
        this.userValidationService = userValidationService1;
        this.awaitingValidationEmailService = awaitingValidationEmailService;
    }
    public Utilisateurs patcherUtilisateur(String idUtilisateur, Utilisateurs partialUpdate) {
        log.info("Patching user with ID: {}", idUtilisateur);
        // 1. Fetch and validate existing user
        UtilisateursEntity existingEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(idUtilisateur)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Utilisateur introuvable avec l'ID: " + idUtilisateur));
        // 2. Map to model for easier manipulation
        Utilisateurs existingUser = mapUtilisateursEntityToModele(existingEntity);

        // 3. Pièces du rôle professeur sur un compte multi-rôles (ex. parent actif qui demande le rôle
        //    professeur, ou compte chargé sous un autre sous-type) : écrites directement dans la table
        //    professeurs, SANS toucher à l'état du compte (le passer en AWAITING_VALIDATION bloquerait
        //    aussi la connexion avec ses autres rôles). La validation admin active ensuite le rôle.
        if (partialUpdate instanceof Professeurs docs && estDemandeRoleProfesseurSupplementaire(existingEntity, existingUser)) {
            return patcherPiecesRoleProfesseur(existingEntity, existingUser, docs);
        }

        // 4. Update common fields with null checks
        updateCommonFields(existingUser, partialUpdate);
        // 5. Handle type-specific updates
        if (existingUser instanceof Professeurs && partialUpdate instanceof Professeurs) {
            log.info("Processing professor update for: {}", existingUser.getEmail());
            Professeurs existingProf = (Professeurs) existingUser;
            EtatUtilisateur originalState = existingProf.getEtat();
            log.info("Original state: {}", originalState);
            boolean pieceRemplacee = pieceRemplacee(existingProf, (Professeurs) partialUpdate);

            handleProfessorUpdates(existingProf, (Professeurs) partialUpdate);
            
            // Check if professor has all documents and should transition to awaiting validation
            boolean hasAllDocuments = existingProf.getCniUrlRecto() != null &&
                    existingProf.getCniUrlVerso() != null &&
                    existingProf.getSelfieUrl() != null;
            boolean wasNotAwaitingValidation = originalState != EtatUtilisateur.AWAITING_VALIDATION;
            
            log.info("Email conditions - hasAllDocuments: {}, wasNotAwaitingValidation: {}", hasAllDocuments, wasNotAwaitingValidation);
            
            // Only a document upload may (re)submit the professor for validation —
            // a plain profile edit (nom, téléphone…) must not demote an ACTIVE
            // professor back to AWAITING_VALIDATION nor re-send the email.
            Professeurs updateProf = (Professeurs) partialUpdate;
            boolean documentsSubmitted = updateProf.getCniUrlRecto() != null
                    || updateProf.getCniUrlVerso() != null
                    || updateProf.getSelfieUrl() != null;

            boolean envoyerEmailAttente = false;
            if (hasAllDocuments && documentsSubmitted && originalState == EtatUtilisateur.ACTIVE) {
                // Compte déjà activé (activation "partielle" par l'administrateur, ou pièces refusées) qui
                // dépose ses pièces : le compte reste ACTIVE (connexion possible) mais le PROFIL professeur
                // passe en EN_ATTENTE_VALIDATION — l'administrateur doit valider les pièces avant que le
                // professeur obtienne ses droits (statut appliqué après la sauvegarde, plus bas).
                existingProf.setHasUploaded(true);
            } else if (hasAllDocuments && documentsSubmitted) {
                existingProf.setHasUploaded(true);
                existingProf.setEtat(EtatUtilisateur.AWAITING_VALIDATION);
                envoyerEmailAttente = true;
            }
            // 6. Map back to entity and save
            UtilisateursEntity updatedEntity = mapUtilisateursModeleToEntity(existingUser);
            updatedEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(updatedEntity);
            // 7. Statut de vérification du profil professeur (SQL natif, après la sauvegarde)
            boolean soumis = documentsSubmitted && appliquerStatutApresDepot(idUtilisateur, hasAllDocuments, pieceRemplacee);
            if (envoyerEmailAttente || soumis) {
                log.info("Sending awaiting validation email for professor: {}", existingProf.getEmail());
                try {
                    awaitingValidationEmailService.sendAwaitingValidationEmail(existingProf);
                } catch (Exception e) {
                    log.warn("Awaiting-validation email not sent to {}: {}", existingProf.getEmail(), e.getMessage());
                }
            }
            if (soumis && notificationService != null) {
                notificationService.createProfessorDocumentsSubmittedNotification(idUtilisateur,
                        (nonNull(existingProf.getPrenom()) + " " + nonNull(existingProf.getNom())).trim());
            }
            return professeurVerification.enrichir(mapUtilisateursEntityToModele(updatedEntity));
        } else if (existingUser instanceof Eleves && partialUpdate instanceof Eleves) {
            handleStudentUpdates((Eleves) existingUser, (Eleves) partialUpdate);
        }
        // 6. Map back to entity and save
        UtilisateursEntity updatedEntity = mapUtilisateursModeleToEntity(existingUser);
        updatedEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(updatedEntity);
        // 7. Return updated model
        return professeurVerification.enrichir(mapUtilisateursEntityToModele(updatedEntity));
    }

    /** Une pièce déjà déposée est-elle remplacée par un AUTRE fichier (renvoyer la même clé n'est pas un remplacement) ? */
    private static boolean pieceRemplacee(Professeurs actuel, Professeurs maj) {
        return remplace(actuel.getCniUrlRecto(), maj.getCniUrlRecto())
                || remplace(actuel.getCniUrlVerso(), maj.getCniUrlVerso())
                || remplace(actuel.getSelfieUrl(), maj.getSelfieUrl());
    }

    private static boolean remplace(String actuelle, String nouvelle) {
        return nonVide(actuelle) != null && nonVide(nouvelle) != null && !nonVide(actuelle).equals(nonVide(nouvelle));
    }

    /** Variante SQL (compte multi-rôles chargé sous un autre sous-type) : compare aux pièces de la ligne professeurs. */
    private boolean piecesDifferentes(String userId, String recto, String verso, String selfie) {
        return userSubtypeService.findSubtype(ProfesseursEntity.class, userId)
                .map(p -> remplace(p.getCniUrlRecto(), recto) || remplace(p.getCniUrlVerso(), verso)
                        || remplace(p.getSelfieUrl(), selfie))
                .orElse(false);
    }

    private static String nonNull(String v) {
        return v == null ? "" : v;
    }

    /**
     * Statut du profil professeur après un dépôt de pièces :
     * <ul>
     *   <li>VALIDE : inchangé, SAUF si une pièce a été remplacée par un nouveau fichier : le profil
     *       repasse en EN_ATTENTE_VALIDATION (droits professeur suspendus via le remapping
     *       ROLE_PROFESSOR_PENDING) jusqu'à ce que l'administrateur valide la mise à jour ;</li>
     *   <li>pièces complètes : EN_ATTENTE_VALIDATION (y compris après un refus : nouvel examen) ;</li>
     *   <li>pièces incomplètes : DOCUMENTS_MANQUANTS (un refus reste affiché tant que le dossier
     *       n'est pas complet).</li>
     * </ul>
     * @return vrai si le dossier vient d'être (re)soumis à l'administrateur
     */
    private boolean appliquerStatutApresDepot(String userId, boolean piecesCompletes, boolean pieceRemplacee) {
        StatutVerificationProfesseur actuel = professeurVerification.statut(userId)
                .orElse(StatutVerificationProfesseur.DOCUMENTS_MANQUANTS);
        if (actuel == StatutVerificationProfesseur.VALIDE) {
            if (!pieceRemplacee || !piecesCompletes) {
                return false;
            }
            daoAccessorService.getRepository(UtilisateursRepository.class)
                    .updateStatutVerificationProfesseur(userId, StatutVerificationProfesseur.EN_ATTENTE_VALIDATION.name(), null);
            log.info("Validated professor {} replaced a verification document — status VALIDE -> EN_ATTENTE_VALIDATION", userId);
            return true;
        }
        UtilisateursRepository repo = daoAccessorService.getRepository(UtilisateursRepository.class);
        if (piecesCompletes) {
            if (actuel == StatutVerificationProfesseur.EN_ATTENTE_VALIDATION) {
                return false; // pièce remplacée pendant l'examen : déjà en attente
            }
            repo.updateStatutVerificationProfesseur(userId, StatutVerificationProfesseur.EN_ATTENTE_VALIDATION.name(), null);
            log.info("Professor {} documents complete — verification status {} -> EN_ATTENTE_VALIDATION", userId, actuel);
            return true;
        }
        if (actuel == StatutVerificationProfesseur.EN_ATTENTE_VALIDATION) {
            repo.updateStatutVerificationProfesseur(userId, StatutVerificationProfesseur.DOCUMENTS_MANQUANTS.name(), null);
        }
        return false;
    }

    private void majStatutVerification(String userId, StatutVerificationProfesseur statut, String motif) {
        daoAccessorService.getRepository(UtilisateursRepository.class)
                .updateStatutVerificationProfesseur(userId, statut.name(), motif);
        log.info("Professor {} verification status -> {}", userId, statut);
    }


    /** Rôle professeur demandé en plus d'un rôle existant et pas encore validé (ou sous-type non chargé). */
    private boolean estDemandeRoleProfesseurSupplementaire(UtilisateursEntity entity, Utilisateurs model) {
        UtilisateursRepository userRepo = daoAccessorService.getRepository(UtilisateursRepository.class);
        if (!userRepo.hasProfesseurRow(entity.getId())) {
            return false;
        }
        if (!(model instanceof Professeurs)) {
            return true; // compte multi-rôles chargé comme parent/élève : on passe par le SQL natif
        }
        return entity.getEtat() == EtatUtilisateur.ACTIVE
                && daoAccessorService.getRepository(UserRoleRepository.class)
                    .findByUtilisateurIdAndRoleType(entity.getId(), "PROFESSOR")
                    .map(r -> !Boolean.TRUE.equals(r.getIsActive()))
                    .orElse(false);
    }

    private static String nonVide(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private Utilisateurs patcherPiecesRoleProfesseur(UtilisateursEntity entity, Utilisateurs existingUser, Professeurs docs) {
        String userId = entity.getId();
        UtilisateursRepository userRepo = daoAccessorService.getRepository(UtilisateursRepository.class);
        String recto = nonVide(docs.getCniUrlRecto());
        String verso = nonVide(docs.getCniUrlVerso());
        String selfie = nonVide(docs.getSelfieUrl());
        String matricule = nonVide(docs.getMatriculeProfesseur());
        for (String url : new String[]{recto, verso, selfie}) {
            if (url != null) validateMediaUrl(url);
        }

        // Champs communs éventuels (nom, téléphone…) — l'état et l'email sont filtrés par UtilisateursService.
        boolean communsModifies = nonVide(docs.getNom()) != null || nonVide(docs.getPrenom()) != null
                || nonVide(docs.getTelephone()) != null || nonVide(docs.getAdresse()) != null
                || nonVide(docs.getEmail()) != null || docs.getEtat() != null;
        if (communsModifies) {
            updateCommonFields(existingUser, docs); // ne lit que les champs communs
            userRepo.save(mapUtilisateursModeleToEntity(existingUser));
        }

        boolean pieceRemplacee = piecesDifferentes(userId, recto, verso, selfie);

        // Après la sauvegarde ci-dessus : la requête native déclenche le flush, puis écrit les pièces.
        userRepo.updateProfesseurDocuments(userId, recto, verso, selfie, matricule);
        boolean apres = userRepo.professeurHasUploaded(userId);
        boolean piecesSoumises = recto != null || verso != null || selfie != null;

        boolean roleEnAttente = daoAccessorService.getRepository(UserRoleRepository.class)
                .findByUtilisateurIdAndRoleType(userId, "PROFESSOR")
                .map(r -> !Boolean.TRUE.equals(r.getIsActive()))
                .orElse(false);
        // Statut du profil professeur : pièces complètes -> EN_ATTENTE_VALIDATION (le rôle reste inactif
        // ou sans droits tant que l'administrateur n'a pas validé).
        boolean soumis = piecesSoumises && appliquerStatutApresDepot(userId, apres, pieceRemplacee);
        if (soumis && entity.getEtat() == EtatUtilisateur.ACTIVE) {
            log.info("Professor role documents complete for existing account {} — awaiting admin validation", userId);
            try {
                awaitingValidationEmailService.sendAwaitingValidationEmail(existingUser);
            } catch (Exception e) {
                log.warn("Awaiting-validation email not sent to {}: {}", entity.getEmail(), e.getMessage());
            }
            if (notificationService != null) {
                String nom = (nonNull(entity.getPrenom()) + " " + nonNull(entity.getNom())).trim();
                if (roleEnAttente) {
                    notificationService.createProfessorRoleRequestedNotification(userId, nom);
                } else {
                    notificationService.createProfessorDocumentsSubmittedNotification(userId, nom);
                }
            }
        }
        return professeurVerification.enrichir(mapUtilisateursEntityToModele(entity));
    }

    private void updateCommonFields(Utilisateurs existing, Utilisateurs updates) {
        if (updates.getNom() != null && !updates.getNom().isBlank()) {
            existing.setNom(updates.getNom().trim());
        }
        if (updates.getPrenom() != null && !updates.getPrenom().isBlank()) {
            existing.setPrenom(updates.getPrenom().trim());
        }
        if (updates.getEmail() != null && !updates.getEmail().isBlank()) {
            existing.setEmail(updates.getEmail().trim().toLowerCase());
        }
        if (updates.getTelephone() != null && !updates.getTelephone().isBlank()) {
            existing.setTelephone(updates.getTelephone().trim());
        }
        if (updates.getAdresse() != null && !updates.getAdresse().isBlank()) {
            existing.setAdresse(updates.getAdresse().trim());
        }
        if (updates.getEtat() != null) {
            existing.setEtat(updates.getEtat());
        }
    }

    private void handleProfessorUpdates(Professeurs existingProf, Professeurs updateProf) {
        // Validate and update CNI Recto
        if (updateProf.getCniUrlRecto() != null) {
            validateMediaUrl(updateProf.getCniUrlRecto());
            existingProf.setCniUrlRecto(updateProf.getCniUrlRecto());
        }
        // Validate and update CNI Verso
        if (updateProf.getCniUrlVerso() != null) {
            validateMediaUrl(updateProf.getCniUrlVerso());
            existingProf.setCniUrlVerso(updateProf.getCniUrlVerso());
        }
        // Validate and update Selfie
        if (updateProf.getSelfieUrl() != null) {
            validateMediaUrl(updateProf.getSelfieUrl());
            existingProf.setSelfieUrl(updateProf.getSelfieUrl());
        }
        // Update matricule if provided
        if (updateProf.getMatriculeProfesseur() != null && !updateProf.getMatriculeProfesseur().isBlank()) {
            existingProf.setMatriculeProfesseur(updateProf.getMatriculeProfesseur().trim());
        }
        // Update hasUploaded status based on document presence
        boolean hasUploaded = existingProf.getCniUrlRecto() != null &&
                existingProf.getCniUrlVerso() != null &&
                existingProf.getSelfieUrl() != null;
        existingProf.setHasUploaded(hasUploaded);
    }

    private void handleStudentUpdates(Eleves existingEleve, Eleves updateEleve) {
        if (updateEleve.getNiveau() != null && !updateEleve.getNiveau().isBlank()) {
            existingEleve.setNiveau(updateEleve.getNiveau().trim());
        }
    }

    private void validateMediaUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "L'URL du média ne peut pas être vide");
        }
        // These fields store the S3 object key/filename the upload flow returns
        // (media.presigned-url), the same convention as MediaEntity.filePath —
        // resolved to a viewable URL on demand via /media/download-by-path.
        // Requiring an absolute URI here rejected every legitimate upload.
    }
    public Utilisateurs avoirUtilisateur(String idUtilisateur) {
        log.info("Récupération de l'utilisateur avec ID: {}", idUtilisateur);
        UtilisateursRepository repo = daoAccessorService.getRepository(UtilisateursRepository.class);
        // Compte multi-rôles avec un profil professeur : on renvoie la vue professeur (sur-ensemble
        // avec les pièces), sinon Hibernate peut charger le compte comme parent/élève et le contrôle
        // "pièces manquantes" du profil professeur se déclencherait à tort.
        UtilisateursEntity utilisateurEntity = null;
        if (repo.hasProfesseurRow(idUtilisateur)) {
            utilisateurEntity = userSubtypeService.findSubtype(ProfesseursEntity.class, idUtilisateur).orElse(null);
        }
        if (utilisateurEntity == null) {
            utilisateurEntity = repo.findById(idUtilisateur)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'ID: " + idUtilisateur));
        }

        // Force loading of received messages
        Hibernate.initialize(utilisateurEntity.getMessagesEnvoyerEntities());

        return professeurVerification.enrichir(mapUtilisateursEntityToModele(utilisateurEntity));
    }


    public Utilisateurs posterUtilisateur(Utilisateurs utilisateur) {
        log.info("Creating new user: {}", utilisateur.getEmail());
        // Check if email already exists - if so, add the new role to existing user
        Optional<UtilisateursEntity> existingUserOpt = utilisateur.getEmail() == null || utilisateur.getEmail().isBlank()
                ? Optional.empty()
                : daoAccessorService.getRepository(UtilisateursRepository.class).findByEmail(utilisateur.getEmail());
        if (existingUserOpt.isPresent()) {
            // Ajout d'un profil à un compte existant : seules les coordonnées déjà enregistrées comptent
            // (ajouterRoleACompteExistant ne lit ni le téléphone ni l'adresse envoyés). Un numéro
            // enregistré avant la validation actuelle (ex. "0123456789") ne doit pas bloquer l'ajout
            // ("Invalid phone number format").
            utilisateur.setTelephone(null);
            userValidationService.validateUserData(utilisateur);
            return ajouterRoleACompteExistant(existingUserOpt.get(), utilisateur);
        }

        // Validation des données
        userValidationService.validateUserData(utilisateur);

        // Configuration par défaut
        utilisateur.setAdmin(false);
        utilisateur.setEtat(utilisateur instanceof Professeurs ?
                EtatUtilisateur.AWAITING_VALIDATION : EtatUtilisateur.PENDING);
        utilisateur.setCreationDate(LocalDateTime.now());

        // For professors, set hasUploaded based on whether documents are provided
        if (utilisateur instanceof Professeurs professeur) {
            boolean hasUploaded = professeur.getCniUrlRecto() != null &&
                    professeur.getCniUrlVerso() != null &&
                    professeur.getSelfieUrl() != null;
            professeur.setHasUploaded(hasUploaded);
        }

        // Mapping et sauvegarde
        UtilisateursEntity userEntity = mapUtilisateursModeleToEntity(utilisateur);
        userEntity.setId(UUID.randomUUID().toString());
        UtilisateursEntity savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(userEntity);

        // Add role to user_roles table. Le rôle PROFESSOR n'est actif qu'après validation admin
        // (validerProfesseur) : la validation se fait au niveau du rôle, pas seulement de l'état du compte,
        // pour qu'un rôle ajouté plus tard (parent…) n'active pas le profil professeur par ricochet.
        String roleType = mapTypeToRole(utilisateur);
        addRoleToUser(savedUserEntity.getId(), roleType, !"PROFESSOR".equals(roleType));
        // Statut de vérification : DOCUMENTS_MANQUANTS par défaut (colonne), EN_ATTENTE_VALIDATION si les
        // pièces ont été fournies dès la création.
        if (savedUserEntity instanceof ProfesseursEntity p && Boolean.TRUE.equals(p.getHasUploaded())) {
            majStatutVerification(savedUserEntity.getId(), StatutVerificationProfesseur.EN_ATTENTE_VALIDATION, null);
        }

        try {
            if (savedUserEntity.getId() != null && !savedUserEntity.getId().equals("temp")) {
                String userFolderPath = "users/" + savedUserEntity.getId();
               // mediaService.ensureFolderExists(userFolderPath);
                log.info("Created user folder for ID: {}", savedUserEntity.getId());
                // Create standard subfolders
                //mediaService.ensureFolderExists(userFolderPath + "/photos");
                //mediaService.ensureFolderExists(userFolderPath + "/documents");
                //mediaService.ensureFolderExists(userFolderPath + "/videos");
            }
        } catch (Exception e) {
            log.error("Failed to create user folder for ID: {}", savedUserEntity.getId(), e);
            // Don't fail the operation, just log the error
        }

        // Send activation email only for non-professors or professors who have uploaded documents
        if (!(savedUserEntity instanceof ProfesseursEntity professeurEntity)) {
            // For non-professors, send activation email immediately
            List<String> roles = roleService.determineUserRoles(mapUtilisateursEntityToModele(savedUserEntity));
            String activationToken = jwtUtil.generateAccessToken(savedUserEntity.getEmail(), roles);
            savedUserEntity.setActivationToken(activationToken);
            savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(savedUserEntity);
            activationEmailService.sendActivationEmail(
                    mapUtilisateursEntityToModele(savedUserEntity),
                    activationToken
            );
        } else if (Boolean.TRUE.equals(professeurEntity.getHasUploaded())) {
            // For professors who already have uploaded documents during creation
            List<String> roles = roleService.determineUserRoles(mapUtilisateursEntityToModele(savedUserEntity));
            String activationToken = jwtUtil.generateAccessToken(savedUserEntity.getEmail(), roles);
            savedUserEntity.setActivationToken(activationToken);
            savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(savedUserEntity);
            activationEmailService.sendActivationEmail(
                    mapUtilisateursEntityToModele(savedUserEntity),
                    activationToken
            );
        }
        // For professors without uploads, DO NOT send any email during creation

        Utilisateurs cree = mapUtilisateursEntityToModele(savedUserEntity);
        cree.setInscriptionStatut(STATUT_CREATED);
        return cree;
    }

    public static final String STATUT_CREATED = "CREATED";
    public static final String STATUT_ROLE_ADDED = "ROLE_ADDED";
    public static final String STATUT_ROLE_PENDING_VALIDATION = "ROLE_PENDING_VALIDATION";
    public static final String STATUT_ACTIVATION_REQUIRED = "ACTIVATION_REQUIRED";

    /**
     * Inscription avec un email déjà utilisé : on ajoute le rôle demandé au compte existant
     * (un même compte peut être professeur ET parent, parent ET élève…).
     * <ul>
     *   <li>élève / parent : rôle actif immédiatement ;</li>
     *   <li>professeur : rôle INACTIF + ligne professeurs ; il faut déposer les pièces puis attendre
     *       la validation admin (validerProfesseur active alors le rôle sans toucher au compte) ;</li>
     *   <li>compte créé comme professeur jamais validé (AWAITING_VALIDATION / REJECTED) ou jamais
     *       activé (PENDING) : le nouveau rôle exige l'activation du compte par email ; le rôle
     *       professeur, lui, reste en attente de validation.</li>
     * </ul>
     */
    private Utilisateurs ajouterRoleACompteExistant(UtilisateursEntity existingEntity, Utilisateurs demande) {
        UtilisateursRepository userRepo = daoAccessorService.getRepository(UtilisateursRepository.class);
        UserRoleRepository roleRepo = daoAccessorService.getRepository(UserRoleRepository.class);
        String userId = existingEntity.getId();
        String newRoleType = mapTypeToRole(demande);
        boolean professeur = "PROFESSOR".equals(newRoleType);

        // Comptes antérieurs à user_roles : synchronise d'abord les rôles déduits des tables filles,
        // sinon un rôle déjà détenu serait "ajouté" une seconde fois.
        if (roleRepo.findByUtilisateurId(userId).isEmpty()) {
            String legacy = mapTypeToRole(mapUtilisateursEntityToModele(existingEntity));
            if (!"USER".equals(legacy)) {
                addRoleToUser(userId, legacy, !"PROFESSOR".equals(legacy) || existingEntity.getEtat() == EtatUtilisateur.ACTIVE);
            }
        }

        Optional<UserRoleEntity> roleExistant = roleRepo.findByUtilisateurIdAndRoleType(userId, newRoleType);
        if (roleExistant.isEmpty()) {
            // Règle de combinaison des profils : le profil élève est exclusif.
            verifierCompatibiliteRole(existingEntity, newRoleType);
        }
        if (roleExistant.isPresent()) {
            // Seule reprise possible : une demande de rôle professeur pas encore validée dont les pièces
            // n'ont pas toutes été déposées (ex. upload interrompu, ou demande refusée) — le client
            // reprend alors l'étape des pièces justificatives.
            boolean reprise = professeur
                    && !Boolean.TRUE.equals(roleExistant.get().getIsActive())
                    && !userRepo.professeurHasUploaded(userId);
            if (!reprise) {
                throw new SchoolException(SchoolErrorCode.DUPLICATE_RESOURCE,
                        professeur && !Boolean.TRUE.equals(roleExistant.get().getIsActive())
                                ? "Votre demande de profil professeur est déjà en attente de validation par l'administration."
                                : "Ce compte possède déjà ce profil. Connectez-vous puis basculez vers ce profil.");
            }
            UserRoleEntity role = roleExistant.get();
            role.setDateAttribution(LocalDateTime.now()); // rouvre la fenêtre de dépôt des pièces
            roleRepo.save(role);
            userRepo.insertProfesseurRole(userId);
            Utilisateurs vue = mapUtilisateursEntityToModele(existingEntity);
            vue.setInscriptionStatut(STATUT_ROLE_PENDING_VALIDATION);
            return vue;
        }

        String statut;
        if (professeur) {
            addRoleToUser(userId, newRoleType, false);
            userRepo.insertProfesseurRole(userId);
            log.info("Added PROFESSOR role as INACTIVE (pending documents + approval) for user {}", userId);
            statut = STATUT_ROLE_PENDING_VALIDATION;
        } else {
            addRoleToUser(userId, newRoleType, true);
            // Lignes des tables filles en SQL natif (l'héritage JOINED empêche de "changer" de sous-type via JPA)
            switch (newRoleType) {
                case "PARENT" -> userRepo.insertParentRole(userId);
                case "STUDENT" -> {
                    String niveau = (demande instanceof Eleves eleve) ? eleve.getNiveau() : null;
                    userRepo.insertEleveRole(userId, niveau != null && !niveau.isBlank() ? niveau : "6eme");
                }
                case "GESTIONNAIRE" -> userRepo.insertGestionnaireRole(userId);
                default -> { }
            }
            statut = STATUT_ROLE_ADDED;

            EtatUtilisateur etat = existingEntity.getEtat();
            if (etat == EtatUtilisateur.AWAITING_VALIDATION || etat == EtatUtilisateur.REJECTED) {
                // Compte créé comme professeur, jamais validé : le nouveau rôle doit être utilisable sans
                // attendre l'admin. Le rôle PROFESSOR reste inactif (validation au niveau du rôle).
                roleRepo.findByUtilisateurIdAndRoleType(userId, "PROFESSOR").ifPresent(r -> {
                    r.setIsActive(false);
                    roleRepo.save(r);
                });
                if (etat == EtatUtilisateur.REJECTED) {
                    userRepo.resetProfesseurDocuments(userId);
                }
                existingEntity.setEtat(EtatUtilisateur.PENDING);
                envoyerEmailActivation(existingEntity);
                statut = STATUT_ACTIVATION_REQUIRED;
            } else if (etat == EtatUtilisateur.PENDING) {
                envoyerEmailActivation(existingEntity);
                statut = STATUT_ACTIVATION_REQUIRED;
            }
        }

        log.info("Added role {} to existing user {} ({})", newRoleType, userId, statut);
        Utilisateurs vue = mapUtilisateursEntityToModele(existingEntity);
        vue.setInscriptionStatut(statut);
        return vue;
    }

    public boolean professeurHasUploaded(String userId) {
        return daoAccessorService.getRepository(UtilisateursRepository.class).professeurHasUploaded(userId);
    }

    private void envoyerEmailActivation(UtilisateursEntity entity) {
        List<String> roles = roleService.determineUserRoles(mapUtilisateursEntityToModele(entity));
        String activationToken = jwtUtil.generateAccessToken(entity.getEmail(), roles);
        entity.setActivationToken(activationToken);
        UtilisateursEntity saved = daoAccessorService.getRepository(UtilisateursRepository.class).save(entity);
        try {
            activationEmailService.sendActivationEmail(mapUtilisateursEntityToModele(saved), activationToken);
        } catch (Exception e) {
            log.error("Activation email could not be sent to {}: {}", entity.getEmail(), e.getMessage());
        }
    }


    public List<Utilisateurs> avoirToutAdmins() {
        log.info("Récupération de tous les administrateurs");
        return daoAccessorService.getRepository(UtilisateursRepository.class).findByAdminTrue()
                .stream().map(UtilisateursBusiness::mapUtilisateursEntityToModele)
                .collect(Collectors.toList());
    }

    public List<Utilisateurs> avoirToutUtilisateurs() {
        log.info("Récupération de tous les utilisateurs");
        return daoAccessorService.getRepository(UtilisateursRepository.class).findAll()
                .stream().map(utilisateursEntity -> mapUtilisateursEntityToModele(utilisateursEntity))
                .collect(Collectors.toList());
    }

    public Utilisateurs mettreUtilisateurAJour(Utilisateurs utilisateur) {
        log.info("Mise à jour de l'utilisateur avec ID: {}", utilisateur.getId());

        // Fetch the existing user entity from the repository
        UtilisateursEntity existingEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(utilisateur.getId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'ID: " + utilisateur.getId()));

        // Map the model to the entity and update the entity
        UtilisateursEntity updatedEntity = mapUtilisateursModeleToEntity(utilisateur);

        // Save the updated entity
        updatedEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(updatedEntity);

        // Return the updated model
        return mapUtilisateursEntityToModele(updatedEntity);
    }

    private static Utilisateurs mapUtilisateursEntityToModele(UtilisateursEntity utilisateurEntity) {
        if(utilisateurEntity instanceof ProfesseursEntity)
            return dozerMapperBean.map(utilisateurEntity, Professeurs.class);
        else if(utilisateurEntity instanceof ElevesEntity)
            return dozerMapperBean.map(utilisateurEntity, Eleves.class);
        else if (utilisateurEntity instanceof RepetiteursEntity)
            return dozerMapperBean.map(utilisateurEntity, Repetiteurs.class);
        else if (utilisateurEntity instanceof ParentsEntity)
            return dozerMapperBean.map(utilisateurEntity, Parents.class);
        else if (utilisateurEntity instanceof GestionnairesEntity)
            return dozerMapperBean.map(utilisateurEntity, Gestionnaires.class);
        else
            return dozerMapperBean.map(utilisateurEntity, Utilisateurs.class);
    }


    public static UtilisateursEntity mapUtilisateursModeleToEntity(IUtilisateurs utilisateur) {
        if (utilisateur instanceof Professeurs)
            return dozerMapperBean.map(utilisateur, ProfesseursEntity.class);
        else if (utilisateur instanceof Eleves)
            return dozerMapperBean.map(utilisateur, ElevesEntity.class);
        else if (utilisateur instanceof Repetiteurs)
            return dozerMapperBean.map(utilisateur, RepetiteursEntity.class);
        else if (utilisateur instanceof Parents)
            return dozerMapperBean.map(utilisateur, ParentsEntity.class);
        else if (utilisateur instanceof Gestionnaires)
            return dozerMapperBean.map(utilisateur, GestionnairesEntity.class);
        else
            return dozerMapperBean.map(utilisateur, UtilisateursEntity.class);
    }


    public Utilisateurs avoirUtilisateurParEmail(String email) {
        log.info("Fetching user with email: {}", email);
        try {
            UtilisateursEntity entity = daoAccessorService.getRepository(UtilisateursRepository.class)
                    .findByEmail(email)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'email: " + email));

            return mapUtilisateursEntityToModele(entity);
        } catch (Exception e) {
            log.error("Error fetching user by email: {}", email, e);
            throw new SchoolException(SchoolErrorCode.EMAIL_ERROR, "Erreur lors de la récupération de l'utilisateur par email");
        }
    }


    public Utilisateurs regenererActivationEmail(String email) {
        log.info("Regenerating activation email for: {}", email);

        UtilisateursEntity utilisateurEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findByEmail(email)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Utilisateur introuvable avec l'email: " + email));

        if (utilisateurEntity.getEtat() != EtatUtilisateur.PENDING) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                    "Activation email can only be regenerated for users in PENDING state");
        }

        // Utilisation du service de rôles
        Utilisateurs utilisateur = mapUtilisateursEntityToModele(utilisateurEntity);
        List<String> roles = roleService.determineUserRoles(utilisateur);

        String activationToken = jwtUtil.generateAccessToken(utilisateurEntity.getEmail(), roles);
        utilisateurEntity.setActivationToken(activationToken);
        utilisateurEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(utilisateurEntity);

        try {
            activationEmailService.sendActivationEmail(utilisateur, activationToken);
            log.info("New activation email sent successfully to {}", utilisateur.getEmail());
        } catch (Exception e) {
            log.error("Failed to send activation email to {}: {}", utilisateur.getEmail(), e.getMessage());
            throw new SchoolException(SchoolErrorCode.EMAIL_NOT_SENT,
                    "L'email d'activation n'a pas pu être envoyé.");
        }

        return utilisateur;
    }




    /**
     * Décision de l'administrateur sur un professeur (POST /utilisateurs/professors/{id}/validate).
     * <ul>
     *   <li>Pièces complètes : profil VALIDE (droits professeur).</li>
     *   <li>Pièces incomplètes : activation "partielle" — le compte (ou le rôle, pour un compte
     *       multi-rôles) est activé pour que le professeur puisse se connecter et déposer ses pièces,
     *       mais le profil reste DOCUMENTS_MANQUANTS : aucun droit professeur tant que l'administrateur
     *       n'a pas validé les pièces (nouvel appel à cette méthode une fois le statut EN_ATTENTE_VALIDATION).</li>
     * </ul>
     * Compte AWAITING_VALIDATION : activation du compte (directe si l'e-mail est vérifié et le mot de passe
     * choisi, sinon e-mail d'activation / choix du mot de passe). Compte ACTIVE : activation du rôle.
     */
    public Utilisateurs validerProfesseur(String professorId) {
        log.info("Processing professor validation for ID: {}", professorId);

        // Fetch the professor by ID — la ligne professeurs est vérifiée en SQL : un compte multi-rôles
        // peut être chargé sous un autre sous-type (parent…) par Hibernate.
        UtilisateursRepository userRepo = daoAccessorService.getRepository(UtilisateursRepository.class);
        UtilisateursEntity userEntity = userRepo
                .findById(professorId)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "Professeur introuvable avec l'ID: " + professorId
                ));

        // Ensure the user is a professor
        if (!userRepo.hasProfesseurRow(professorId)) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_OPERATION,
                    "L'utilisateur n'est pas un professeur"
            );
        }

        StatutVerificationProfesseur statutActuel = professeurVerification.statut(professorId)
                .orElse(StatutVerificationProfesseur.DOCUMENTS_MANQUANTS);
        boolean piecesCompletes = userRepo.professeurHasUploaded(professorId);
        StatutVerificationProfesseur nouveauStatut = piecesCompletes
                ? StatutVerificationProfesseur.VALIDE : StatutVerificationProfesseur.DOCUMENTS_MANQUANTS;

        // Compte déjà actif : activation partielle déjà faite (professeur), ou compte parent/élève… qui a
        // demandé le rôle professeur. Le compte et son mot de passe restent inchangés.
        if (userEntity.getEtat() == EtatUtilisateur.ACTIVE) {
            UserRoleRepository roleRepo = daoAccessorService.getRepository(UserRoleRepository.class);
            boolean roleActif = roleRepo.findByUtilisateurIdAndRoleType(professorId, "PROFESSOR")
                    .map(r -> Boolean.TRUE.equals(r.getIsActive()))
                    .orElse(true); // compte antérieur à user_roles : rôle déduit du sous-type
            if (roleActif && statutActuel == StatutVerificationProfesseur.VALIDE) {
                throw new SchoolException(SchoolErrorCode.INVALID_STATE, "Le profil professeur de ce compte est déjà validé");
            }
            if (roleActif && !piecesCompletes) {
                throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                        "Ce professeur peut déjà se connecter mais n'a pas encore déposé toutes ses pièces "
                                + "(CNI recto, CNI verso et selfie) : il n'y a rien à valider pour le moment.");
            }
            activateRoleForUser(professorId, "PROFESSOR");
            majStatutVerification(professorId, nouveauStatut, null);
            if (notificationService != null) {
                if (!roleActif && nouveauStatut == StatutVerificationProfesseur.VALIDE) {
                    // Rôle supplémentaire d'un compte existant : message "basculez vers ce profil"
                    notificationService.createRoleRequestDecisionNotification(professorId, "professeur", true, null);
                } else {
                    notificationService.createProfessorVerificationNotification(professorId, nouveauStatut.name(), null);
                }
            }
            Utilisateurs valide = mapUtilisateursEntityToModele(userEntity);
            log.info("PROFESSOR profile of active account {} -> {}", professorId, nouveauStatut);
            return professeurVerification.enrichir(valide);
        }

        // Ensure the professor is in the correct state
        if (userEntity.getEtat() != EtatUtilisateur.AWAITING_VALIDATION) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_STATE,
                    "Le professeur n'est pas en attente de validation"
            );
        }

        // Change the state to 'PENDING' (not 'VALIDATED' directly)
        userEntity.setEtat(EtatUtilisateur.PENDING);

        // Generate activation token with professor's email, not admin's
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_PROFESSOR");

        String activationToken = jwtUtil.generateAccessToken(userEntity.getEmail(), roles); // Utilisez l'email du professeur ici
        userEntity.setActivationToken(activationToken);

        // Save the validated professor entity
        userEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(userEntity);

        // Activate the PROFESSOR role in user_roles table
        activateRoleForUser(professorId, "PROFESSOR");
        majStatutVerification(professorId, nouveauStatut, null);
        if (notificationService != null) {
            notificationService.createProfessorVerificationNotification(professorId, nouveauStatut.name(), null);
        }

        // Convert the entity to model and send activation email
        Utilisateurs utilisateur = mapUtilisateursEntityToModele(userEntity);
        activationEmailService.sendActivationEmail(utilisateur, activationToken);

        log.info("Professor {} account validated (activation e-mail sent), profile {}", professorId, nouveauStatut);

        return professeurVerification.enrichir(utilisateur);
    }

    public List<Utilisateurs> avoirProfesseursEnAttente() {
        log.info("Fetching all professors awaiting validation");

        // Comptes professeur en AWAITING_VALIDATION + comptes actifs ayant demandé le rôle professeur
        // (pièces déposées, rôle encore inactif). Chargés via ProfesseursRepository pour avoir les pièces
        // même si le compte possède d'autres sous-types.
        List<String> ids = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findProfesseurIdsEnAttenteDeValidation();
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        return daoAccessorService.getRepository(ProfesseursRepository.class).findAllById(ids).stream()
                .map(p -> {
                    Utilisateurs u = professeurVerification.enrichir(mapUtilisateursEntityToModele(p));
                    // Pour l'écran admin, c'est le PROFIL professeur qui est en attente de validation
                    // (statutVerification distingue un compte déjà actif dont les pièces sont à examiner).
                    u.setEtat(EtatUtilisateur.AWAITING_VALIDATION);
                    return u;
                })
                .collect(Collectors.toList());
    }



    public Utilisateurs rejeterProfesseur(String professorId, String codeErreur, String motifSupplementaire) {
        log.info("Rejet du professeur avec l'ID: {}", professorId);

        // Récupérer le professeur
        ProfesseursEntity professeurEntity = userSubtypeService.findSubtype(ProfesseursEntity.class, professorId)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "Professeur introuvable avec l'ID: " + professorId
                ));

        // Vérifiez que le token est bien présent
        if (professeurEntity.getActivationToken() == null) {
            professeurEntity.setActivationToken(jwtUtil.generateRefreshToken(professeurEntity.getEmail()));
        }
        // Compte actif (parent, élève…) dont la demande de rôle professeur est refusée : le compte reste
        // actif avec ses autres rôles ; le rôle professeur reste inactif et les pièces sont effacées
        // (la demande pourra être refaite).
        if (professeurEntity.getEtat() == EtatUtilisateur.ACTIVE) {
            boolean roleEnAttente = daoAccessorService.getRepository(UserRoleRepository.class)
                    .findByUtilisateurIdAndRoleType(professorId, "PROFESSOR")
                    .map(r -> !Boolean.TRUE.equals(r.getIsActive()))
                    .orElse(false);
            StatutVerificationProfesseur statutActuel = professeurVerification.statut(professorId)
                    .orElse(StatutVerificationProfesseur.DOCUMENTS_MANQUANTS);
            if (!roleEnAttente && statutActuel == StatutVerificationProfesseur.VALIDE) {
                throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                        "Le profil professeur de ce compte est déjà validé et ne peut pas être rejeté ici.");
            }
            MotifRejetEntity motif = daoAccessorService.getRepository(MotifRejetRepository.class)
                    .findByCode(codeErreur)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                            "Motif de rejet introuvable avec le code: " + codeErreur));
            String motifTexte = libelleMotif(motif, motifSupplementaire);
            if (roleEnAttente) {
                // Compte parent/élève… : la demande de rôle est refusée, les pièces sont effacées
                // (la demande pourra être refaite).
                daoAccessorService.getRepository(UtilisateursRepository.class).resetProfesseurDocuments(professorId);
            }
            // Compte professeur activé partiellement : il reste connecté (compte ACTIVE) pour consulter le
            // motif et redéposer ses pièces ; ses droits professeur restent bloqués (profil REJETE).
            majStatutVerification(professorId, StatutVerificationProfesseur.REJETE, motifTexte);
            try {
                rejectionEmailService.sendRejectionEmail(professeurEntity, motif, motifSupplementaire);
            } catch (Exception e) {
                log.warn("Rejection email not sent for {}: {}", professorId, e.getMessage());
            }
            if (notificationService != null) {
                if (roleEnAttente) {
                    notificationService.createRoleRequestDecisionNotification(professorId, "professeur", false, motifTexte);
                } else {
                    notificationService.createProfessorVerificationNotification(professorId,
                            StatutVerificationProfesseur.REJETE.name(), motifTexte);
                }
            }
            return dozerMapperBean.map(professeurEntity, Utilisateurs.class);
        }

        // Vérifier que le professeur est en attente de validation
        if (professeurEntity.getEtat() != EtatUtilisateur.AWAITING_VALIDATION) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_STATE,
                    "Le professeur doit être en attente de validation pour être rejeté. Statut actuel: " + professeurEntity.getEtat()
            );
        }

        // Récupérer le motif de rejet
        MotifRejetEntity motifEntity = daoAccessorService.getRepository(MotifRejetRepository.class)
                .findByCode(codeErreur)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "Motif de rejet introuvable avec le code: " + codeErreur
                ));

        // Mettre à jour le statut du professeur
        professeurEntity.setEtat(EtatUtilisateur.REJECTED);
        ProfesseursEntity savedEntity = daoAccessorService.getRepository(ProfesseursRepository.class)
                .save(professeurEntity);
        majStatutVerification(professorId, StatutVerificationProfesseur.REJETE, libelleMotif(motifEntity, motifSupplementaire));

        // Envoyer l'email de rejet via le service dédié
        rejectionEmailService.sendRejectionEmail(professeurEntity, motifEntity, motifSupplementaire);

        return dozerMapperBean.map(savedEntity, Utilisateurs.class);
    }

    private static String libelleMotif(MotifRejetEntity motif, String motifSupplementaire) {
        String base = motif.getDescriptif() != null && !motif.getDescriptif().isBlank() ? motif.getDescriptif() : motif.getCode();
        if (motifSupplementaire != null && !motifSupplementaire.isBlank()) {
            return base + " — " + motifSupplementaire.trim();
        }
        return base;
    }

    /**
     * Get all roles for a user from user_roles table (including inactive ones)
     */
    public List<String> getAllUserRoleTypes(String userId) {
        try {
            UserRoleRepository roleRepo = daoAccessorService.getRepository(UserRoleRepository.class);
            return roleRepo.findByUtilisateurId(userId).stream()
                    .map(UserRoleEntity::getRoleType)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch all user roles for {}: {}", userId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Map user type to role string
     */
    private String mapTypeToRole(Utilisateurs utilisateur) {
        if (utilisateur instanceof Professeurs) return "PROFESSOR";
        if (utilisateur instanceof Eleves) return "STUDENT";
        if (utilisateur instanceof Parents) return "PARENT";
        if (utilisateur instanceof Repetiteurs) return "TUTOR";
        if (utilisateur instanceof Gestionnaires) return "GESTIONNAIRE";
        if (utilisateur.isAdmin()) return "ADMIN";
        return "USER";
    }

    /**
     * Get all active roles for a user from user_roles table
     */
    public List<String> getUserRoles(String userId) {
        try {
            UserRoleRepository roleRepo = daoAccessorService.getRepository(UserRoleRepository.class);
            // Ordre d'attribution : le premier rôle obtenu est le profil proposé par défaut.
            return roleRepo.findByUtilisateurIdAndIsActiveTrue(userId).stream()
                    .sorted(java.util.Comparator.comparing(UserRoleEntity::getDateAttribution,
                            java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())))
                    .map(UserRoleEntity::getRoleType)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch user roles for {}: {}", userId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /** Rôles demandés mais pas encore utilisables (ex. PROFESSOR en attente de validation). */
    public List<String> getPendingRoles(String userId) {
        try {
            return daoAccessorService.getRepository(UserRoleRepository.class).findByUtilisateurId(userId).stream()
                    .filter(r -> !Boolean.TRUE.equals(r.getIsActive()))
                    .map(UserRoleEntity::getRoleType)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch pending roles for {}: {}", userId, e.getMessage());
            return new ArrayList<>();
        }
    }

    public static final String MSG_ELEVE_EXCLUSIF =
            "Un compte élève ne peut pas avoir d'autre profil (parent ou professeur). "
                    + "Utilisez une autre adresse e-mail pour créer ce profil.";
    public static final String MSG_ELEVE_NON_AJOUTABLE =
            "Un profil élève ne peut pas être ajouté à un compte qui possède déjà un autre profil "
                    + "(parent, professeur…). Utilisez une autre adresse e-mail pour créer un compte élève.";

    /**
     * Règle de combinaison des profils : un compte élève (STUDENT) ne détient aucun autre profil, et le
     * profil élève ne peut pas être ajouté à un compte existant d'un autre type. Parent et professeur
     * (ainsi que les autres profils) restent combinables entre eux. Les rôles en attente (professeur non
     * validé) comptent comme détenus.
     *
     * @throws SchoolException ROLE_INCOMPATIBLE (HTTP 409) si la combinaison est interdite
     */
    public void verifierCompatibiliteRole(UtilisateursEntity compte, String nouveauRole) {
        if (compte == null || nouveauRole == null) {
            return;
        }
        java.util.Set<String> roles = new java.util.HashSet<>(getAllUserRoleTypes(compte.getId()));
        String legacy = mapTypeToRole(mapUtilisateursEntityToModele(compte));
        if (!"USER".equals(legacy)) {
            roles.add(legacy);
        }
        roles.remove("USER");
        roles.remove(nouveauRole);
        if (roles.isEmpty()) {
            return;
        }
        if (roles.contains("STUDENT")) {
            throw new SchoolException(SchoolErrorCode.ROLE_INCOMPATIBLE, MSG_ELEVE_EXCLUSIF);
        }
        if ("STUDENT".equals(nouveauRole)) {
            throw new SchoolException(SchoolErrorCode.ROLE_INCOMPATIBLE, MSG_ELEVE_NON_AJOUTABLE);
        }
    }

    /** Variante par identifiant (ajouts de rôle hors inscription, ex. demande d'accès parent). */
    public void verifierCompatibiliteRole(String userId, String nouveauRole) {
        daoAccessorService.getRepository(UtilisateursRepository.class).findById(userId)
                .ifPresent(u -> verifierCompatibiliteRole(u, nouveauRole));
    }

    /**
     * Add a role to an existing user
     */
    public void addRoleToUser(String userId, String roleType) {
        addRoleToUser(userId, roleType, true);
    }

    /**
     * Add a role to an existing user with specified active state.
     * For PROFESSOR role added to existing users, active should be false (pending approval).
     */
    public void addRoleToUser(String userId, String roleType, boolean active) {
        UserRoleRepository roleRepo = daoAccessorService.getRepository(UserRoleRepository.class);
        if (!roleRepo.existsByUtilisateurIdAndRoleType(userId, roleType)) {
            UserRoleEntity role = new UserRoleEntity();
            role.setId(UUID.randomUUID().toString());
            role.setUtilisateurId(userId);
            role.setRoleType(roleType);
            role.setIsActive(active);
            role.setDateAttribution(LocalDateTime.now());
            roleRepo.save(role);
            log.info("Added role {} to user {} (active={})", roleType, userId, active);
        }
    }

    /**
     * Activate a specific role for a user (e.g., after admin approval)
     */
    public void activateRoleForUser(String userId, String roleType) {
        UserRoleRepository roleRepo = daoAccessorService.getRepository(UserRoleRepository.class);
        Optional<UserRoleEntity> roleOpt = roleRepo.findByUtilisateurIdAndRoleType(userId, roleType);
        if (roleOpt.isPresent()) {
            UserRoleEntity role = roleOpt.get();
            role.setIsActive(true);
            roleRepo.save(role);
            log.info("Activated role {} for user {}", roleType, userId);
        } else {
            // Role doesn't exist yet, create it as active
            addRoleToUser(userId, roleType, true);
        }
    }

    /**
     * Get children for a parent from parent_eleve table
     */
    public List<AuthResponse.ChildInfo> getChildrenForParent(String parentId) {
        try {
            // Lecture directe de parent_eleve : ne dépend pas du sous-type sous lequel le compte
            // (professeur ET parent, par ex.) a été chargé dans cette requête.
            List<String> ids = daoAccessorService.getRepository(ParentEleveRepository.class).findEleveIdsByParentId(parentId);
            if (ids.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            return daoAccessorService.getRepository(ElevesRepository.class).findAllById(ids).stream()
                    .map(e -> AuthResponse.ChildInfo.builder()
                            .id(e.getId())
                            .nom(e.getNom())
                            .prenom(e.getPrenom())
                            .niveau(e.getNiveau())
                            .build())
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch children for parent {}: {}", parentId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}
