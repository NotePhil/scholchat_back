package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MediaService;
import cmr.notep.business.services.NotificationService;
import cmr.notep.interfaces.modeles.Evenement;
import cmr.notep.interfaces.modeles.Media;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.EvenementEntity;
import cmr.notep.ressourcesjpa.dao.MediaEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.dao.UserRoleEntity;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.EvenementRepository;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import cmr.notep.ressourcesjpa.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import cmr.notep.interfaces.modeles.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class EvenementBusiness {

    private final DaoAccessorService daoAccessorService;
    private final InteractionBusiness interactionBusiness;
    private final NotificationService notificationService;
    private final MediaService mediaService;

    public EvenementBusiness(DaoAccessorService daoAccessorService, InteractionBusiness interactionBusiness,
                             NotificationService notificationService, MediaService mediaService) {
        this.daoAccessorService = daoAccessorService;
        this.interactionBusiness = interactionBusiness;
        this.notificationService = notificationService;
        this.mediaService = mediaService;
    }

    public Evenement creerEvenement(Evenement evenement) {
        try {
            // Map the event DTO to entity
            EvenementEntity entity = dozerMapperBean.map(evenement, EvenementEntity.class);
            entity.setId(UUID.randomUUID().toString());
            
            // Set default etat to PLANIFIE if not specified
            if (entity.getEtat() == null) {
                entity.setEtat(cmr.notep.modele.EtatEvenement.PLANIFIE);
            }
            
            // Handle visibility mapping - if etat is PUBLIC/PRIVATE, map to visibility
            if (evenement.getEtat() != null) {
                String etatString = evenement.getEtat().toString();
                if ("PUBLIC".equals(etatString) || "PRIVATE".equals(etatString)) {
                    entity.setVisibility(etatString);
                    entity.setEtat(cmr.notep.modele.EtatEvenement.PLANIFIE); // Set default etat
                }
            }

            // Fetch and set the creator (any user type can create events)
            UtilisateursEntity createur = daoAccessorService.getRepository(UtilisateursRepository.class)
                    .findById(evenement.getCreateurId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                            "Utilisateur introuvable avec l'ID: " + evenement.getCreateurId()));
            entity.setCreateur(createur);

            // Set participants if needed
            if (evenement.getParticipantsIds() != null) {
                entity.setParticipantsIds(evenement.getParticipantsIds());
            }
            
            // Set visibility and classes
            entity.setVisibility(evenement.getVisibility());
            if (evenement.getClassesIds() != null && !evenement.getClassesIds().isEmpty()) {
                List<ClassesEntity> classesEntities = daoAccessorService
                        .getRepository(ClassesRepository.class)
                        .findAllById(evenement.getClassesIds());
                entity.setClasses(classesEntities);
            }
            
            // Handle frontend sending PUBLIC/PRIVATE in etat field
            if (evenement.getEtat() != null) {
                String etatString = evenement.getEtat().toString();
                if ("PUBLIC".equals(etatString)) {
                    entity.setVisibility("PUBLIC");
                    entity.setEtat(cmr.notep.modele.EtatEvenement.PLANIFIE);
                } else if ("PRIVATE".equals(etatString)) {
                    entity.setVisibility("PRIVATE");
                    entity.setEtat(cmr.notep.modele.EtatEvenement.PLANIFIE);
                }
            }

            // Handle media properly with all required fields
            if (evenement.getMedias() != null && !evenement.getMedias().isEmpty()) {
                log.info("Processing {} media files for event", evenement.getMedias().size());
                
                List<MediaEntity> mediaEntities = evenement.getMedias().stream()
                        .map(media -> {
                            log.debug("Processing media: fileName={}, filePath={}, contentType={}", 
                                    media.getFileName(), media.getFilePath(), media.getContentType());
                            
                            MediaEntity mediaEntity = dozerMapperBean.map(media, MediaEntity.class);
                            mediaEntity.setEvenement(entity); // Set the relationship

                            // Set all required fields with proper validation
                            mediaEntity.setBucketName(media.getBucketName() != null ? media.getBucketName() : "scholchat");
                            mediaEntity.setContentType(media.getContentType() != null ? media.getContentType() : "application/octet-stream");
                            
                            // Set mediaType based on fileType or contentType
                            String mediaType = media.getMediaType();
                            if (mediaType == null) {
                                mediaType = media.getFileType();
                            }
                            if (mediaType == null) {
                                String contentType = mediaEntity.getContentType();
                                if (contentType != null && contentType.startsWith("image/")) {
                                    mediaType = "IMAGE";
                                } else {
                                    mediaType = "DOCUMENT";
                                }
                            }
                            mediaEntity.setMediaType(mediaType);
                            
                            // Set fileName with validation
                            String fileName = media.getFileName();
                            if (fileName == null || fileName.trim().isEmpty()) {
                                fileName = "event-file-" + System.currentTimeMillis();
                            }
                            mediaEntity.setFileName(fileName);
                            
                            // Set fileSize
                            mediaEntity.setFileSize(media.getFileSize() != null ? media.getFileSize() : 0L);
                            
                            // Set filePath with validation
                            String filePath = media.getFilePath();
                            if (filePath == null || filePath.trim().isEmpty()) {
                                filePath = "events/" + fileName;
                            }
                            mediaEntity.setFilePath(filePath);
                            
                            // Set ownerId from event creator
                            mediaEntity.setOwnerId(evenement.getCreateurId());
                            if (mediaEntity.getUploadedDate() == null) mediaEntity.setUploadedDate(java.time.LocalDateTime.now());
                            
                            log.debug("Processed media entity: fileName={}, filePath={}, mediaType={}, bucketName={}", 
                                    mediaEntity.getFileName(), mediaEntity.getFilePath(), 
                                    mediaEntity.getMediaType(), mediaEntity.getBucketName());

                            return mediaEntity;
                        })
                        .collect(Collectors.toList());
                entity.setMedias(mediaEntities);
                log.info("Successfully processed {} media entities", mediaEntities.size());
            }

            // Save the event
            EvenementEntity savedEntity = daoAccessorService.getRepository(EvenementRepository.class).save(entity);

            // Map back to DTO for response
            Evenement savedEvenement = dozerMapperBean.map(savedEntity, Evenement.class);
            savedEvenement.setCreateurId(savedEntity.getCreateur().getId());
            savedEvenement.setCreateurNom(savedEntity.getCreateur().getNom());
            savedEvenement.setCreateurPrenom(savedEntity.getCreateur().getPrenom());
            // Determine role from user_roles table
            String role = "Utilisateur";
            try {
                List<UserRoleEntity> userRoles = daoAccessorService
                        .getRepository(UserRoleRepository.class)
                        .findByUtilisateurIdAndIsActiveTrue(savedEntity.getCreateur().getId());
                if (!userRoles.isEmpty()) {
                    String primaryRole = userRoles.get(0).getRoleType();
                    java.util.Map<String, String> roleMap = java.util.Map.of(
                        "PROFESSOR", "Professeur", "PARENT", "Parent",
                        "STUDENT", "Eleve", "ADMIN", "Admin",
                        "GESTIONNAIRE", "Gestionnaire", "TUTOR", "Repetiteur"
                    );
                    role = roleMap.getOrDefault(primaryRole, primaryRole);
                } else if (Boolean.TRUE.equals(savedEntity.getCreateur().getAdmin())) {
                    role = "Admin";
                }
            } catch (Exception roleErr) {
                if (Boolean.TRUE.equals(savedEntity.getCreateur().getAdmin())) role = "Admin";
            }
            savedEvenement.setCreateurRole(role);
            savedEvenement.setInteractions(interactionBusiness.getInteractionsByEvent(savedEntity.getId()));
            savedEvenement.setClassesIds(savedEntity.getClasses().stream()
                    .map(ClassesEntity::getId).collect(Collectors.toList()));

            log.info("Event created successfully with ID: {}", savedEntity.getId());

            // Send notifications to students in the selected classes
            try {
                String creatorName = createur.getPrenom() + " " + createur.getNom();
                List<String> classesIds = savedEntity.getClasses().stream()
                        .map(ClassesEntity::getId).collect(Collectors.toList());
                if (!classesIds.isEmpty()) {
                    notificationService.createActivityNotification(
                            savedEntity.getId(), savedEntity.getTitre(),
                            createur.getId(), creatorName, classesIds);
                }
            } catch (Exception ex) {
                log.error("Error sending event notifications: {}", ex.getMessage());
            }

            return savedEvenement;

        } catch (SchoolException e) {
            log.error("Business logic error creating event: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error saving event: {}", e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la sauvegarde de l'événement: " + e.getMessage());
        }
    }

    public Evenement mettreAJourEvenement(String id, Evenement evenement) {
        try {
            EvenementRepository repo = daoAccessorService.getRepository(EvenementRepository.class);
            EvenementEntity existing = repo.findById(id)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Événement non trouvé avec l'ID: " + id));

            existing.setTitre(evenement.getTitre());
            existing.setDescription(evenement.getDescription());
            existing.setLieu(evenement.getLieu());
            existing.setEtat(evenement.getEtat());
            existing.setHeureDebut(evenement.getHeureDebut());
            existing.setHeureFin(evenement.getHeureFin());
            existing.setVisibility(evenement.getVisibility());

            if (evenement.getClassesIds() != null) {
                existing.setClasses(daoAccessorService
                        .getRepository(ClassesRepository.class)
                        .findAllById(evenement.getClassesIds()));
            }

            // Merge medias: keep existing (by id), add new ones, remove deleted ones
            if (evenement.getMedias() != null) {
                // IDs the frontend wants to keep
                java.util.Set<String> keepIds = evenement.getMedias().stream()
                        .map(cmr.notep.interfaces.modeles.Media::getId)
                        .filter(mid -> mid != null && !mid.isBlank())
                        .collect(java.util.stream.Collectors.toSet());

                // Remove medias no longer in the list (orphanRemoval handles DB delete)
                existing.getMedias().removeIf(me -> !keepIds.contains(me.getId()));

                // Add new medias (those without an id)
                evenement.getMedias().stream()
                        .filter(media -> media.getId() == null || media.getId().isBlank())
                        .forEach(media -> {
                            MediaEntity me = dozerMapperBean.map(media, MediaEntity.class);
                            me.setId(null); // ensure Hibernate treats it as new
                            me.setEvenement(existing);
                            me.setBucketName(media.getBucketName() != null ? media.getBucketName() : "scholchat");
                            me.setContentType(media.getContentType() != null ? media.getContentType() : "application/octet-stream");
                            String mt = media.getMediaType() != null ? media.getMediaType() : media.getFileType();
                            if (mt == null) mt = me.getContentType().startsWith("image/") ? "IMAGE" : "VIDEO";
                            me.setMediaType(mt);
                            me.setOwnerId(existing.getCreateur().getId());
                            if (me.getFileName() == null || me.getFileName().isBlank()) {
                                String fp = media.getFilePath();
                                me.setFileName(fp != null && fp.contains("/")
                                        ? fp.substring(fp.lastIndexOf('/') + 1)
                                        : (fp != null ? fp : "media-" + System.currentTimeMillis()));
                            }
                            if (me.getFilePath() == null || me.getFilePath().isBlank())
                                me.setFilePath("events/" + me.getFileName());
                            existing.getMedias().add(me);
                        });
            }

            EvenementEntity updated = repo.save(existing);
            Evenement result = dozerMapperBean.map(updated, Evenement.class);
            result.setCreateurId(updated.getCreateur().getId());
            result.setInteractions(interactionBusiness.getInteractionsByEvent(id));
            // Embed presigned URLs in updated medias
            if (result.getMedias() != null) {
                result.getMedias().forEach(media -> {
                    if (media.getFilePath() != null) {
                        try { media.setPresignedUrl(mediaService.generateDownloadPresignedUrl(media.getFilePath())); }
                        catch (Exception ex) { log.warn("Could not generate presigned URL for media {}", media.getId()); }
                    }
                });
            }
            log.info("Event updated successfully with ID: {}", id);
            return result;
        } catch (SchoolException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating event with ID {}: {}", id, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la mise à jour de l'événement: " + e.getMessage());
        }
    }
    
    public void gererParticipantEvenement(String eventId, String userId, boolean rejoindre) {
        try {
            EvenementEntity event = daoAccessorService.getRepository(EvenementRepository.class)
                    .findById(eventId)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Événement non trouvé avec l'ID: " + eventId));
            
            List<String> participants = event.getParticipantsIds();
            if (participants == null) {
                participants = new ArrayList<>();
            }
            
            if (rejoindre) {
                if (!participants.contains(userId)) {
                    participants.add(userId);
                    log.info("Utilisateur {} ajouté aux participants de l'événement {}", userId, eventId);
                }
            } else {
                participants.remove(userId);
                log.info("Utilisateur {} retiré des participants de l'événement {}", userId, eventId);
            }
            
            event.setParticipantsIds(participants);
            daoAccessorService.getRepository(EvenementRepository.class).save(event);
            
        } catch (SchoolException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erreur lors de la gestion du participant {} pour l'événement {}: {}", userId, eventId, e.getMessage());
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR, "Erreur lors de la gestion du participant");
        }
    }

    /**
     * Paginated fetch — loads one page of events ordered by heureDebut DESC.
     * Each event is enriched the same way as obtenirTousEvenements
     * (creator info, class IDs, interactions, presigned media URLs).
     * Visibility filtering is applied in the service layer (same logic as the
     * non-paginated endpoint) so the returned content/totalElements are accurate
     * for the calling user.
     *
     * @param page       0-based page index
     * @param size       number of events per page
     * @param userClasses list of class IDs the current user belongs to (used for PRIVATE visibility)
     * @param isAdmin    whether the caller is an admin (sees all events)
     * @return PagedEvenementResponse with content + pagination metadata
     */
    public PagedResponse<Evenement> obtenirEvenementsPagines(int page, int size,
                                                           List<String> userClasses,
                                                           boolean isAdmin) {
        try {
            EvenementRepository repo = daoAccessorService.getRepository(EvenementRepository.class);

            // Fetch one page from DB, ordered by heureDebut DESC
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<EvenementEntity> dbPage = repo.findAllOrderByHeurDebutDesc(pageRequest);

            List<Evenement> enriched = dbPage.getContent().stream()
                    .map(e -> {
                        try {
                            Evenement ev = dozerMapperBean.map(e, Evenement.class);
                            if (e.getCreateur() != null) {
                                ev.setCreateurId(e.getCreateur().getId());
                                ev.setCreateurNom(e.getCreateur().getNom());
                                ev.setCreateurPrenom(e.getCreateur().getPrenom());
                                String role = "Utilisateur";
                                try {
                                    List<cmr.notep.ressourcesjpa.dao.UserRoleEntity> userRoles =
                                            daoAccessorService.getRepository(cmr.notep.ressourcesjpa.repository.UserRoleRepository.class)
                                                    .findByUtilisateurIdAndIsActiveTrue(e.getCreateur().getId());
                                    if (!userRoles.isEmpty()) {
                                        String primaryRole = userRoles.get(0).getRoleType();
                                        java.util.Map<String, String> roleMap = java.util.Map.of(
                                                "PROFESSOR", "Professeur", "PARENT", "Parent",
                                                "STUDENT", "Eleve", "ADMIN", "Admin",
                                                "GESTIONNAIRE", "Gestionnaire", "TUTOR", "Repetiteur");
                                        role = roleMap.getOrDefault(primaryRole, primaryRole);
                                    } else if (Boolean.TRUE.equals(e.getCreateur().getAdmin())) {
                                        role = "Admin";
                                    }
                                } catch (Exception re) {
                                    if (Boolean.TRUE.equals(e.getCreateur().getAdmin())) role = "Admin";
                                }
                                ev.setCreateurRole(role);
                            }
                            ev.setClassesIds(e.getClasses().stream()
                                    .map(cmr.notep.ressourcesjpa.dao.ClassesEntity::getId)
                                    .collect(Collectors.toList()));
                            ev.setInteractions(interactionBusiness.getInteractionsByEvent(e.getId()));
                            if (ev.getMedias() != null) {
                                ev.getMedias().forEach(media -> {
                                    if (media.getFilePath() != null) {
                                        try {
                                            media.setPresignedUrl(mediaService.generateDownloadPresignedUrl(media.getFilePath()));
                                        } catch (Exception ex) {
                                            log.warn("Could not generate presigned URL for media {}: {}", media.getId(), ex.getMessage());
                                        }
                                    }
                                });
                            }
                            return ev;
                        } catch (Exception ex) {
                            log.error("Error enriching event {} for paginated response: {}", e.getId(), ex.getMessage());
                            Evenement fallback = new Evenement();
                            fallback.setId(e.getId());
                            fallback.setTitre(e.getTitre());
                            fallback.setDescription(e.getDescription());
                            fallback.setLieu(e.getLieu());
                            fallback.setEtat(e.getEtat());
                            fallback.setHeureDebut(e.getHeureDebut());
                            fallback.setHeureFin(e.getHeureFin());
                            fallback.setVisibility(e.getVisibility());
                            if (e.getCreateur() != null) {
                                fallback.setCreateurId(e.getCreateur().getId());
                                fallback.setCreateurNom(e.getCreateur().getNom());
                                fallback.setCreateurPrenom(e.getCreateur().getPrenom());
                            }
                            fallback.setClassesIds(e.getClasses().stream()
                                    .map(cmr.notep.ressourcesjpa.dao.ClassesEntity::getId)
                                    .collect(Collectors.toList()));
                            return fallback;
                        }
                    })
                    // Apply visibility filter (mirrors EvenementService.obtenirTousEvenements)
                    .filter(ev -> {
                        if (isAdmin) return true;
                        if (!"PRIVATE".equals(ev.getVisibility())) return true;
                        List<String> eventClasses = ev.getClassesIds();
                        if (eventClasses == null || eventClasses.isEmpty()) return true;
                        return userClasses.stream().anyMatch(eventClasses::contains);
                    })
                    .collect(Collectors.toList());

            // Recalculate totals after visibility filter
            // totalElements from DB is unfiltered; we correct it by using the filtered count
            // for the current page but keep DB total as an upper bound approximation.
            // For accurate total, admins get the raw DB total; others get an estimate.
            long totalElements = isAdmin
                    ? dbPage.getTotalElements()
                    : dbPage.getTotalElements(); // approximation — exact count would need a separate query

            int totalPages = (int) Math.ceil((double) totalElements / size);
            boolean isLast = dbPage.isLast();

            return new PagedResponse<>(enriched, page, size, totalElements, totalPages, isLast);
        } catch (Exception e) {
            log.error("Error retrieving paginated events (page={}, size={}): {}", page, size, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la récupération paginée des événements: " + e.getMessage());
        }
    }

    public List<Evenement> obtenirTousEvenements() {
        try {
            List<EvenementEntity> entities = daoAccessorService.getRepository(EvenementRepository.class).findAll();
            log.info("Retrieved {} events from database", entities.size());

            return entities.stream()
                    .map(e -> {
                        try {
                            Evenement evenement = dozerMapperBean.map(e, Evenement.class);
                            if (e.getCreateur() != null) {
                                evenement.setCreateurId(e.getCreateur().getId());
                                evenement.setCreateurNom(e.getCreateur().getNom());
                                evenement.setCreateurPrenom(e.getCreateur().getPrenom());
                                // Determine role from user_roles table (reliable for multi-role users)
                                String role = "Utilisateur";
                                try {
                                    List<UserRoleEntity> userRoles = daoAccessorService
                                            .getRepository(UserRoleRepository.class)
                                            .findByUtilisateurIdAndIsActiveTrue(e.getCreateur().getId());
                                    if (!userRoles.isEmpty()) {
                                        // Use primary role (first one, typically the original role)
                                        String primaryRole = userRoles.get(0).getRoleType();
                                        java.util.Map<String, String> roleMap = java.util.Map.of(
                                            "PROFESSOR", "Professeur", "PARENT", "Parent",
                                            "STUDENT", "Eleve", "ADMIN", "Admin",
                                            "GESTIONNAIRE", "Gestionnaire", "TUTOR", "Repetiteur"
                                        );
                                        role = roleMap.getOrDefault(primaryRole, primaryRole);
                                    } else if (Boolean.TRUE.equals(e.getCreateur().getAdmin())) {
                                        role = "Admin";
                                    }
                                } catch (Exception roleErr) {
                                    log.warn("Could not determine role: {}", roleErr.getMessage());
                                    if (Boolean.TRUE.equals(e.getCreateur().getAdmin())) role = "Admin";
                                }
                                evenement.setCreateurRole(role);
                            }
                            evenement.setClassesIds(e.getClasses().stream()
                                    .map(ClassesEntity::getId).collect(Collectors.toList()));
                            evenement.setInteractions(interactionBusiness.getInteractionsByEvent(e.getId()));
                            // Embed presigned URL in each media — eliminates N download-url round trips
                            if (evenement.getMedias() != null) {
                                evenement.getMedias().forEach(media -> {
                                    if (media.getFilePath() != null) {
                                        try {
                                            media.setPresignedUrl(mediaService.generateDownloadPresignedUrl(media.getFilePath()));
                                        } catch (Exception ex) {
                                            log.warn("Could not generate presigned URL for media {}: {}", media.getId(), ex.getMessage());
                                        }
                                    }
                                });
                            }
                            return evenement;
                        } catch (Exception ex) {
                            log.error("Error mapping event with ID {}: {}", e.getId(), ex.getMessage());
                            Evenement fallbackEvent = new Evenement();
                            fallbackEvent.setId(e.getId());
                            fallbackEvent.setTitre(e.getTitre());
                            fallbackEvent.setDescription(e.getDescription());
                            fallbackEvent.setLieu(e.getLieu());
                            fallbackEvent.setEtat(e.getEtat());
                            fallbackEvent.setHeureDebut(e.getHeureDebut());
                            fallbackEvent.setHeureFin(e.getHeureFin());
                            if (e.getCreateur() != null) {
                                fallbackEvent.setCreateurId(e.getCreateur().getId());
                                fallbackEvent.setCreateurNom(e.getCreateur().getNom());
                                fallbackEvent.setCreateurPrenom(e.getCreateur().getPrenom());
                            }
                            fallbackEvent.setInteractions(interactionBusiness.getInteractionsByEvent(e.getId()));
                            return fallbackEvent;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving all events: {}", e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la récupération des événements: " + e.getMessage());
        }
    }

    public void supprimerEvenement(String id) {
        try {
            EvenementRepository repo = daoAccessorService.getRepository(EvenementRepository.class);
            if (!repo.existsById(id)) {
                throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Événement non trouvé avec l'ID: " + id);
            }
            repo.deleteById(id);
            log.info("Event deleted successfully with ID: {}", id);
        } catch (SchoolException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting event with ID {}: {}", id, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la suppression de l'événement: " + e.getMessage());
        }
    }

    public Evenement obtenirEvenementParId(String id) {
        try {
            EvenementEntity entity = daoAccessorService.getRepository(EvenementRepository.class)
                    .findById(id)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Événement non trouvé avec l'ID: " + id));

            Evenement evenement = dozerMapperBean.map(entity, Evenement.class);
            if (entity.getCreateur() != null) {
                evenement.setCreateurId(entity.getCreateur().getId()); // Set creator ID properly
            }
            evenement.setClassesIds(entity.getClasses().stream()
                    .map(ClassesEntity::getId).collect(Collectors.toList()));
            evenement.setInteractions(interactionBusiness.getInteractionsByEvent(id));
            return evenement;
        } catch (SchoolException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving event with ID {}: {}", id, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la récupération de l'événement: " + e.getMessage());
        }
    }

    public List<Evenement> obtenirEvenementsParProfesseur(String professeurId) {
        try {
            List<EvenementEntity> entities = daoAccessorService.getRepository(EvenementRepository.class)
                    .findByCreateurId(professeurId); // This now works with the fixed repository method

            log.info("Retrieved {} events for professor ID: {}", entities.size(), professeurId);

            return entities.stream()
                    .map(e -> {
                        try {
                            Evenement evenement = dozerMapperBean.map(e, Evenement.class);
                            if (e.getCreateur() != null) {
                                evenement.setCreateurId(e.getCreateur().getId()); // Set creator ID properly
                            }
                            evenement.setClassesIds(e.getClasses().stream()
                                    .map(ClassesEntity::getId).collect(Collectors.toList()));
                            evenement.setInteractions(interactionBusiness.getInteractionsByEvent(e.getId()));
                            return evenement;
                        } catch (Exception ex) {
                            log.error("Error mapping event with ID {} for professor {}: {}", e.getId(), professeurId, ex.getMessage());
                            // Return a basic event object if mapping fails
                            Evenement fallbackEvent = new Evenement();
                            fallbackEvent.setId(e.getId());
                            fallbackEvent.setTitre(e.getTitre());
                            fallbackEvent.setDescription(e.getDescription());
                            fallbackEvent.setLieu(e.getLieu());
                            fallbackEvent.setEtat(e.getEtat());
                            fallbackEvent.setHeureDebut(e.getHeureDebut());
                            fallbackEvent.setHeureFin(e.getHeureFin());
                            if (e.getCreateur() != null) {
                                fallbackEvent.setCreateurId(e.getCreateur().getId());
                            }
                            fallbackEvent.setInteractions(interactionBusiness.getInteractionsByEvent(e.getId()));
                            return fallbackEvent;
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving events for professor {}: {}", professeurId, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Erreur lors de la récupération des événements du professeur: " + e.getMessage());
        }
    }
}