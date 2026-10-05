package cmr.notep.business.business;

import cmr.notep.business.security.UserSubtypeService;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.ActivationEmailService;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import cmr.notep.ressourcesjpa.repository.DroitPublicationRepository;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@Transactional
public class ProfesseursBusiness {
    private final UserSubtypeService userSubtypeService;
    private final DaoAccessorService daoAccessorService;
    private final ActivationEmailService activationEmailService;
    private final JwtUtil jwtUtil;
    private final cmr.notep.business.services.NotificationService notificationService;

    public ProfesseursBusiness(DaoAccessorService daoAccessorService, ActivationEmailService activationEmailService, JwtUtil jwtUtil, cmr.notep.business.services.NotificationService notificationService,
            UserSubtypeService userSubtypeService) {
        this.userSubtypeService = userSubtypeService;
        this.daoAccessorService = daoAccessorService;
        this.activationEmailService = activationEmailService;
        this.jwtUtil = jwtUtil;
        this.notificationService = notificationService;
    }

    public Professeurs avoirProfesseur(String idProfesseur) {
        ProfesseursEntity entity = userSubtypeService.findSubtype(ProfesseursEntity.class, idProfesseur)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable avec l'ID: " + idProfesseur));

        Professeurs professeur = dozerMapperBean.map(entity, Professeurs.class);

        // Map moderated classes to simple IDs
        professeur.setModeratedClasses(
                entity.getModeratedClasses().stream()
                        .map(c -> {
                            Classes cls = new Classes();
                            cls.setId(c.getId());
                            cls.setNom(c.getNom());
                            return cls;
                        })
                        .collect(Collectors.toList())
        );

        return professeur;
    }

    public Professeurs posterProfesseur(Professeurs professeur) {
        Professeurs savedProfesseur = dozerMapperBean.map(
                this.daoAccessorService.getRepository(ProfesseursRepository.class)
                        .save(dozerMapperBean.map(professeur, ProfesseursEntity.class)),
                Professeurs.class
        );
        
        // Notify admins about new professor registration
        notificationService.createProfessorCreatedNotification(savedProfesseur.getId(), savedProfesseur.getNom() + " " + savedProfesseur.getPrenom());
        
        return savedProfesseur;
    }

    public Professeurs modifierProfesseurPartiellement(String idProfesseur, Professeurs partialUpdate) {
        ProfesseursRepository repository = daoAccessorService.getRepository(ProfesseursRepository.class);

        // Get existing professor
        ProfesseursEntity existingEntity = userSubtypeService.findSubtype(ProfesseursEntity.class, idProfesseur)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable avec l'ID: " + idProfesseur));

        // Map the existing entity to model
        Professeurs existingProfesseur = dozerMapperBean.map(existingEntity, Professeurs.class);

        // Apply partial updates
        if (partialUpdate.getNom() != null) {
            existingProfesseur.setNom(partialUpdate.getNom());
        }
        if (partialUpdate.getPrenom() != null) {
            existingProfesseur.setPrenom(partialUpdate.getPrenom());
        }
        if (partialUpdate.getEmail() != null) {
            existingProfesseur.setEmail(partialUpdate.getEmail());
        }
        if (partialUpdate.getTelephone() != null) {
            existingProfesseur.setTelephone(partialUpdate.getTelephone());
        }
        if (partialUpdate.getAdresse() != null) {
            existingProfesseur.setAdresse(partialUpdate.getAdresse());
        }
        if (partialUpdate.getEtat() != null) {
            existingProfesseur.setEtat(partialUpdate.getEtat());
        }
        if (partialUpdate.getCniUrlRecto() != null) {
            existingProfesseur.setCniUrlRecto(partialUpdate.getCniUrlRecto());
        }
        if (partialUpdate.getCniUrlVerso() != null) {
            existingProfesseur.setCniUrlVerso(partialUpdate.getCniUrlVerso());
        }
        if (partialUpdate.getSelfieUrl() != null) {
            existingProfesseur.setSelfieUrl(partialUpdate.getSelfieUrl());
        }
        if (partialUpdate.getMatriculeProfesseur() != null) {
            existingProfesseur.setMatriculeProfesseur(partialUpdate.getMatriculeProfesseur());
        }

        // Update hasUploaded status based on document presence
        boolean hasUploaded = existingProfesseur.getCniUrlRecto() != null &&
                existingProfesseur.getCniUrlVerso() != null &&
                existingProfesseur.getSelfieUrl() != null;
        existingProfesseur.setHasUploaded(hasUploaded);

        // Save the updated entity
        ProfesseursEntity updatedEntity = repository.save(dozerMapperBean.map(existingProfesseur, ProfesseursEntity.class));

        // Statut de vérification : pièces (re)déposées et complètes -> à examiner par l'administrateur
        // (un profil déjà VALIDE le reste).
        boolean piecesSoumises = partialUpdate.getCniUrlRecto() != null || partialUpdate.getCniUrlVerso() != null
                || partialUpdate.getSelfieUrl() != null;
        if (piecesSoumises && hasUploaded) {
            cmr.notep.ressourcesjpa.repository.UtilisateursRepository userRepo =
                    daoAccessorService.getRepository(cmr.notep.ressourcesjpa.repository.UtilisateursRepository.class);
            String statut = userRepo.findStatutVerificationProfesseur(idProfesseur).orElse(null);
            if ("DOCUMENTS_MANQUANTS".equals(statut) || "REJETE".equals(statut)) {
                userRepo.updateStatutVerificationProfesseur(idProfesseur, "EN_ATTENTE_VALIDATION", null);
            }
        }

        // Check if we need to send activation email after upload
        boolean wasNotUploaded = !existingEntity.getHasUploaded();
        boolean nowHasUploaded = hasUploaded;

        if (wasNotUploaded && nowHasUploaded) {
            // Professor just completed uploads - send activation email
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_PROFESSOR");

            String activationToken = jwtUtil.generateAccessToken(existingProfesseur.getEmail(), roles);
            existingProfesseur.setActivationToken(activationToken);

            // Update entity with activation token
            updatedEntity.setActivationToken(activationToken);
            updatedEntity = repository.save(updatedEntity);

            // Send activation email
            activationEmailService.sendActivationEmail(existingProfesseur, activationToken);
            
            // Notify admins that a professor is ready for validation (documents uploaded)
            notificationService.createProfessorCreatedNotification(existingProfesseur.getId(), existingProfesseur.getNom() + " " + existingProfesseur.getPrenom() + " (Documents téléchargés)");
        }

        return dozerMapperBean.map(updatedEntity, Professeurs.class);
    }
    public List<Professeurs> avoirToutProfesseurs() {
        return daoAccessorService.getRepository(ProfesseursRepository.class).findAll()
                .stream()
                .map(prof -> dozerMapperBean.map(prof, Professeurs.class))
                .collect(Collectors.toList());
    }

    public Professeurs avoirProfesseurParMatricule(String matriculeProfesseur) {
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ProfesseursRepository.class)
                        .findByMatriculeProfesseur(matriculeProfesseur),
                Professeurs.class
        );
    }

    /** Professors who have droit_publication on a given class */
    public List<Professeurs> avoirProfesseursParClasse(String classeId) {
        return daoAccessorService.getRepository(DroitPublicationRepository.class)
                .findAllUsersByClassId(classeId)
                .stream()
                .map(d -> d.getUtilisateur())
                .filter(u -> u != null && userSubtypeService.isProfesseur(u.getId()))
                .map(u -> dozerMapperBean.map(u, Professeurs.class))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns all professors who have droit_publication on any class
     * moderated by the given professor — excluding the moderator himself.
     */
    public List<Professeurs> avoirCollaborateursProfesseur(String moderateurId) {
        if (!userSubtypeService.isProfesseur(moderateurId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable");
        }

        List<ClassesEntity> classesModerees = daoAccessorService.getRepository(ClassesRepository.class)
                .findByModeratorId(moderateurId);
        if (classesModerees.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        return classesModerees.stream()
                .flatMap(classe -> daoAccessorService.getRepository(DroitPublicationRepository.class)
                        .findAllUsersByClassId(classe.getId()).stream()
                        .map(d -> d.getUtilisateur())
                        .filter(u -> u != null && !u.getId().equals(moderateurId)
                                && userSubtypeService.isProfesseur(u.getId())))
                .distinct()
                .map(this::toShallowProfesseur)
                .collect(Collectors.toList());
    }

    private Professeurs toShallowProfesseur(UtilisateursEntity e) {
        Professeurs p = new Professeurs();
        p.setId(e.getId());
        p.setNom(e.getNom());
        p.setPrenom(e.getPrenom());
        p.setEmail(e.getEmail());
        p.setTelephone(e.getTelephone());
        p.setAdresse(e.getAdresse());
        p.setEtat(e.getEtat());
        // Compte multi-rôles : l'entité peut être chargée sous un autre sous-type -> colonnes lues en natif
        if (e instanceof ProfesseursEntity prof) {
            p.setMatriculeProfesseur(prof.getMatriculeProfesseur());
            p.setHasUploaded(Boolean.TRUE.equals(prof.getHasUploaded()));
        } else {
            UtilisateursRepository repo = daoAccessorService.getRepository(UtilisateursRepository.class);
            p.setMatriculeProfesseur(repo.findMatriculeProfesseur(e.getId()).orElse(null));
            p.setHasUploaded(repo.professeurHasUploaded(e.getId()));
        }
        return p;
    }
}